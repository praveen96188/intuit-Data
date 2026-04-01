
./create_CDC_C3-D3.sh psppp01 psphpp06 task7a arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y
./create_CDC_C3-D3.sh psppp01 psphpp06 task7b arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y
./create_CDC_C3-D3.sh psppp01 psphpp06 task7c arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y
./create_CDC_C3-D3.sh psppp01 psphpp06 task7d arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y



./create_CDC_C3-D3.sh psppp01 psphpp06 task1a arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y
./create_CDC_C3-D3.sh psppp01 psphpp06 task1b arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y
./create_CDC_C3-D3.sh psppp01 psphpp06 task1c arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y




ssh -o ServerAliveInterval=45 -i ~/.ssh/id_rsa -L 127.0.0.1:10567:psphpp06.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521 pnarlagalla@ec2-52-38-226-158.us-west-2.compute.amazonaws.com
