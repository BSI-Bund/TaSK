## Frequently asked questions

#### Why are there no tshark logs?
Not every testcase makes use of tshark. But if it does, there should be a *.pcap file in the report folder. 
1. Check whether tshark is installed in your system and make sure, that the path is correctly written in your global configuration file.
Usually it is `/usr/bin/tshark`.
2. Possibly you need to add the user executing TaSK to the group `wireshark`. (execute `usermod -a -G wireshark <YOUR_USERNAME>` as admin user).
3. Check the correct configuration of the interface id with the list given by `tshark -D`
