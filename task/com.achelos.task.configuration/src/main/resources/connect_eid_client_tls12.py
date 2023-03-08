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


def main():
    parser = argparse.ArgumentParser(description="connect_eid_client_tls12")
    parser.add_argument("-eid_client_port", type=int, default=24727,
                        help="Port on which the eID-Client listens for commands. Default:24727")
    parser.add_argument("-eservice_hostname", required=True,
                        help="HostName of the eID-Server to include in TCToken.")
    parser.add_argument("-eservice_port", required=True,
                        help="Port of the eID-Server to include in TCToken.")
    parser.add_argument("-browsersimclient", required=True,
                        help="Absolute path to browsersimulatorclient.jar")
    parser.add_argument("-browsersim_hostname", required=True,
                        help="Hostname of the browsersimulator RMI service")
    parser.add_argument("-browsersim_port", required=True,
                        help="Port of the browsersimulator RMI service")

    args = parser.parse_args()

    prefix = "DUT log: "

    # Use the BrowserSimulator to start the eID Client and tell it to connect to eID Service
    browsersimulator_result = connect_browsersimulator(args.browsersimclient, args.browsersim_hostname, args.browsersim_port, args.eid_client_port, args.eservice_hostname, args.eservice_port)

    exit(0)


def connect_browsersimulator(browsersimclient: str, browsersim_hostname :str, browsersim_port :int, eid_client_port: int, eservice_hostname: str, eservice_port: int):
    try:
        url = f'http://127.0.0.1:{eid_client_port}/eID-Client?tcTokenURL=https://{eservice_hostname}:{eservice_port}'
        command = f'java -jar {browsersimclient} {browsersim_hostname} {browsersim_port} {url}'
        result = os.system(command)
        return result
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")


if __name__ == "__main__":
    main()
