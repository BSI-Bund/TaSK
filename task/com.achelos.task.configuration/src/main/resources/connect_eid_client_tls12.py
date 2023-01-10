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
    parser.add_argument("-eid_client", required=True,
                        help="Path to the eID Client Executable.")
    parser.add_argument("-eid_client_port", type=int, default=24727,
                        help="Port on which the eID-Client listens for commands. Default:24727")
    parser.add_argument("-eservice_hostname", required=True,
                        help="HostName of the eID-Server to include in TCToken.")
    parser.add_argument("-eservice_port", required=True,
                        help="Port of the eID-Server to include in TCToken.")

    args = parser.parse_args()

    prefix = "DUT log: "

    # Starting eID Client
    print(f"{prefix}Starting eID Client.")
    eid_client_process = subprocess.Popen(args.eid_client, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        preexec_fn=os.setsid, universal_newlines=True)
    time.sleep(2)

    # Get eID Client to connect to eID Service
    connect_eid_client(args.eid_client_port, args.eservice_hostname, args.eservice_port)

    print(f"{prefix}Waiting for eID Client to finish.")
    time.sleep(5)

    print(f"{prefix}Exiting.")
    os.killpg(os.getpgid(eid_client_process.pid), signal.SIGTERM)

    print(f"{prefix}Saving logs.")
    print(eid_client_process.stdout.read())

    exit(0)


def connect_eid_client(eid_client_port: int, eservice_hostname: str, eservice_port: int):
    try:
        command = f'http://127.0.0.1:{eid_client_port}/eID-Client?tcTokenURL=https://{eservice_hostname}:{eservice_port}'
        print(f"DUT log: Sending command '{command}' to eID Client to connect.")
        response = requests.get(f'{command}')
        print(f"Returned status code of eID-Client to the browser: {response.status_code}")
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")


if __name__ == "__main__":
    main()
