import base64
import binascii
import sys, getopt, glob

def main(argv):
	inputfolder = ''
	opts, args = getopt.getopt(argv,"hi:",["ifolder="])
	for opt, arg in opts:
		if opt == '-h':
			print ('manipulateLastByte.py -i <inputFolder>')
			sys.exit()
		elif opt in ("-i", "--ifolder"):
			inputfolder = arg

 	# Step 1: Get the list of PEM file paths
	certList = glob.glob(inputfolder + '/test_server_*_certificate.pem')
	
	for pem_path in certList:
		print ("Manipulating:", pem_path)
		# Step 2: Read the PEM file contents and decode from base64
		with open(pem_path, "rb") as pem_file:
			pem_contents = pem_file.read()
			pem_base64List = pem_contents.split(b"\n")  # Remove header and footer
			#find if END CERTIFICATE
			endIndex = -1
			for index, item in reversed(list(enumerate(pem_base64List))):
				if b"END CERTIFICATE" in item:
					endIndex = index
					break
			
			pem_base64 = pem_base64List[1:endIndex]
			pem_bytes = base64.b64decode(b"==".join(pem_base64))

	 	# Step 3: Manipulate the last byte of the hex value
		hex_string = binascii.hexlify(pem_bytes)
		last_byte = hex_string[-2:]
		new_last_byte = b"55"
		new_hex_string = hex_string[:-2] + new_last_byte
		# Step 4: Convert the new hex string to base64
		new_pem_bytes = binascii.unhexlify(new_hex_string)
		new_pem_base64 = base64.b64encode(new_pem_bytes).decode()

		# Step 5: Write the new base64 value back to the PEM file
		new_pem_contents = b"-----BEGIN CERTIFICATE-----\n" + \
	 			   new_pem_base64.encode() + \
	 			   b"\n-----END CERTIFICATE-----\n"
		with open(pem_path, "wb") as pem_file:
			pem_file.write(new_pem_contents)
	
if __name__ == "__main__":
	main(sys.argv[1:])
	
