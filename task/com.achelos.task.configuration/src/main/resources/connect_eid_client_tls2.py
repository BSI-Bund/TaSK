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
    parser.add_argument("-eid_client", required=True,
                        help="Path to the eID Client Executable.")
    parser.add_argument("-eservice_port", type=int, default=8447, nargs="?", help="Port Number on which the mock eService should listen. Default: 8443")
    parser.add_argument("-eid_client_port", type=int, default=24727,
                        help="Port on which the eID-Client listens for commands. Default:24727")
    parser.add_argument("-eid_server_hostname", required=True,
                        help="HostName of the eID-Server to include in TCToken.")
    parser.add_argument("-eid_server_port", required=True,
                        help="Port of the eID-Server to include in TCToken.")
    parser.add_argument("-psk", required=True,
                        help="PSK to include in TCToken.")

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

    prefix = "DUT log: "
    # Starting eID Client
    print(f"{prefix}Starting eID Client.")
    eid_client_process = subprocess.Popen(args.eid_client, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        preexec_fn=os.setsid, universal_newlines=True)


    #print(f"{prefix}Waiting for eID Client to start.")
    time.sleep(2)

    # Get eID Client to connect to eID Service
    connect_eid_client(args.eid_client_port, args.eservice_port)

    print(f"{prefix}Waiting for eID Client to finish.")
    time.sleep(5)

    print(f"{prefix}Exiting.")
    eid_service_thread.terminate()
    eid_service_thread.join()
    os.killpg(os.getpgid(eid_client_process.pid), signal.SIGTERM)

    print(f"{prefix}Saving logs.")
    print(eid_client_process.stdout.read())

    exit(0)


def start_server(cert_path, key_path, port: int):
    try:
        httpd = HTTPServer(("localhost", port), TCTokenHTTPRequestHandler)
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.load_cert_chain(certfile=cert_path, keyfile=key_path)
        httpd.socket = context.wrap_socket(httpd.socket,
                                           server_side=True,
                                           do_handshake_on_connect=True)
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")
    print(f"DUT log: Serving HTTPS on localhost port {port}.")
    httpd.serve_forever()


def connect_eid_client(eid_client_port: int, eservice_port: int):
    try:
        command = f'http://127.0.0.1:{eid_client_port}/eID-Client?tcTokenURL=https://localhost:{eservice_port}'
        print(f"DUT log: Sending command '{command}' to eID Client to connect.")
        response = requests.get(f'{command}')
        print(f"Returned status code of eID-Client to the browser: {response.status_code}")
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