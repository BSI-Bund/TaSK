#!/usr/bin/python3
from jpype import getDefaultJVMPath, startJVM, shutdownJVM, java, addClassPath, JClass
import multiprocessing
import signal
import subprocess
from http.server import HTTPServer, BaseHTTPRequestHandler
import argparse
import os
import ssl
import requests
import time


JVM_PATH = '/usr/lib/jvm/java-11-openjdk-amd64/lib/server/libjvm.so'

def main():
    parser = argparse.ArgumentParser(description="connect_eid_client_tls12")
    parser.add_argument("-eid_client_port", type=int, default=24727,
                        help="Port on which the eID-Client listens for commands. Default:24727")
    parser.add_argument("-eservice_hostname", required=True,
                        help="HostName of the eID-Server to include in TCToken.")
    parser.add_argument("-eservice_port", required=True,
                        help="Port of the eID-Server to include in TCToken.")
    #parser.add_argument("-browsersim_hostname", required=True,
    #                    help="Hostname for the BrowserSimulator RMI Server")
    #parser.add_argument("-browsersim_port", type=int, default=1099,
    #                    help="Port for the BrowserSimulator RMI Server")
    parser.add_argument("-browsersimclient", required=True,
                        help="Absolute path to browsersimulatorRemote.jar")

    args = parser.parse_args()

    prefix = "DUT log: "


    # Use Browsersimulator to connect to eID Service
    run_browsersimulator(args.browsersimclient, args.eid_client_port, args.eservice_hostname, args.eservice_port)

    print(f"{prefix}Waiting for eID-Client to finish.")
    time.sleep(10)

    print(f"{prefix}Exiting.")

    print(f"{prefix}Saving logs.")

    exit(0)


def run_browsersimulator(browsersimclient: str, eid_client_port: int, eservice_hostname: str, eservice_port: int):
    try:
        url = f'http://127.0.0.1:{eid_client_port}/eID-Client?tcTokenURL=https://{eservice_hostname}:{eservice_port}'
        #jvmpath = getDefaultJVMPath()
        jvmpath = '/usr/lib/jvm/java-11-openjdk-amd64/lib/server/libjvm.so'
        #print(jvmpath)
        javaclasspath = f'-Djava.class.path={browsersimclient}'

        startJVM(JVM_PATH, "-ea", javaclasspath)

        browsersim = JClass("com.achelos.task.BrowserSimulatorRemote2")
        browsersim.main([f'{url}'])
        
        browsersim.startApp()
        time.sleep(4)

        browsersim.sendRequest()
        time.sleep(4)

        browsersim.stopApp()
        time.sleep(2)

        #browsersim.readLogs()

        shutdownJVM()
    except Exception as e:
        exit(f"DUT log: Error occurred: {str(e)}")


if __name__ == "__main__":
    main()
