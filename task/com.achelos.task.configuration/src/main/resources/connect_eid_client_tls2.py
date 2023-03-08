#!/usr/bin/python3
import multiprocessing
import signal
import subprocess
from http.server import HTTPServer, BaseHTTPRequestHandler
import argparse
import os
import ssl
import requests
import time

TC_TOKEN = ""


def main():
    parser = argparse.ArgumentParser(description="connect_eid_client_tls2")
    parser.add_argument("-server_cert", required=True,
                        help="Path to Server Certificate which shall be used for the eService mock.")
    parser.add_argument("-server_key", required=True,
                        help="Path to Server Private Key which shall be used for the eService mock.")
    parser.add_argument("-eservice_port", type=int, default=8447, nargs="?", help="Port Number on which the mock eService should listen. Default: 8443")
    parser.add_argument("-eid_client_port", type=int, default=24727,
                        help="Port on which the eID-Client listens for commands. Default:24727")
    parser.add_argument("-eid_server_hostname", required=True,
                        help="HostName of the eID-Server to include in TCToken.")
    parser.add_argument("-eid_server_port", required=True,
                        help="Port of the eID-Server to include in TCToken.")
    parser.add_argument("-psk", required=True,
                        help="PSK to include in TCToken.")
    parser.add_argument("-browsersimclient", required=True,
                        help="Absolute path to browsersimulatorclient.jar")
    parser.add_argument("-browsersim_hostname", required=True,
                        help="Hostname of the browsersimulator RMI service")
    parser.add_argument("-browsersim_port", required=True,
                        help="Port of the browsersimulator RMI service")

    args = parser.parse_args()

    # Set TC Token global variable
    global TC_TOKEN
    TC_TOKEN = f"""<TCTokenType>
        <ServerAddress>https://{args.eid_server_hostname}:{args.eid_server_port}</ServerAddress>
        <SessionIdentifier>Client_identity</SessionIdentifier>
        <RefreshAddress>https://www.bsi.bund.de</RefreshAddress>
        <CommunicationErrorAddress>https://www.bsi.bund.de</CommunicationErrorAddress>
        <Binding>urn:liberty:paos:2006-08</Binding>
        <PathSecurity-Protocol>urn:ietf:rfc:4279</PathSecurity-Protocol>
        <PathSecurity-Parameters>
            <PSK>{args.psk}</PSK>
        </PathSecurity-Parameters>
    </TCTokenType>"""

    # Starting eID Service
    eid_service_thread = multiprocessing.Process(target=start_server,
                                                 args=(args.server_cert, args.server_key, args.eservice_port),
                                                 daemon=True)
    eid_service_thread.start()
    time.sleep(2)
    prefix = "DUT log: "

    # Use the BrowserSimulator to start the eID Client and tell it to connect to eID Service
    browsersimulator_result = connect_browsersimulator(args.browsersimclient,  args.browsersim_hostname, args.browsersim_port, args.eid_client_port, args.eservice_port, args.eid_server_hostname)

    print(f"{prefix}Exiting.")
    eid_service_thread.terminate()
    eid_service_thread.join()

    exit(0)


def start_server(cert_path, key_path, port: int):
    try:
        httpd = HTTPServer(('', port), TCTokenHTTPRequestHandler)
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.load_cert_chain(certfile=cert_path, keyfile=key_path)
        httpd.socket = context.wrap_socket(httpd.socket,
                                           server_side=True,
                                           do_handshake_on_connect=True)
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")
    print(f"DUT log: Serving HTTPS on localhost port {port}.")
    httpd.serve_forever()


def connect_browsersimulator(browsersimclient: str, browsersim_hostname :str, browsersim_port :int, eid_client_port: int, eservice_port: int, eservice_hostname : str):
    try:
        url = f'http://127.0.0.1:{eid_client_port}/eID-Client?tcTokenURL=https://{eservice_hostname}:{eservice_port}'
        command = f'java -jar {browsersimclient} {browsersim_hostname} {browsersim_port} {url}'
        result = os.system(command)
        return result
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")


class TCTokenHTTPRequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/xml; charset=utf-8")
        self.send_header("Content-Length", str(len(TC_TOKEN)))
        self.end_headers()
        self.wfile.write(TC_TOKEN.encode("UTF-8"))

if __name__ == "__main__":
    main()
