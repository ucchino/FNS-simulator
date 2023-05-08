#------------------------------------------------------------------------------------------------
# - To access AWS services create a USER (call it FNS), dont' care the region USER IS GLOBAL
#    https://us-east-1.console.aws.amazon.com/iamv2/home#/users
#------------------------------------------------------------------------------------------------
# - Add policies https://us-east-1.console.aws.amazon.com/iamv2/home?region=eu-central-1#/users/details/FNS/add-permissions
#	1: AdministratorAccess
#	2: AmazonEC2FullAccess
#	3: AmazonS3FullAccess
#	4: AmazonKinesisFullAccess
#	5: AmazonKinesisFirehoseFullAccess
#------------------------------------------------------------------------------------------------
# - Add access key (KEY IS GLOBAL) 
#    https://us-east-1.console.aws.amazon.com/iamv2/home#/users/details/FNS/create-access-key
#    use case: Servizio di terze parti - Description FNS_TERRAFORM
#------------------------------------------------------------------------------------------------

#------------------------------------------------------------------------------------------------
#4 - Create and download EC2 access key (KEY IS LOCAL FOR REGION) to access ec2 : fns_keypair_region :
#    https://eu-central-1.console.aws.amazon.com/ec2/home?region=eu-central-1#KeyPairs:
#	 see: https://www.sammeechward.com/terraform-vpc-subnets-ec2-and-more
#------------------------------------------------------------------------------------------------
# VAR - FYI to use provisioner "file" -> connect, to upload a file for example: convert key in PEM format: GUI Start PuTTYgen.
#	Load your private key in .ppk format. Then go to Menu > Conversions > Export > OpenSSH. 
#	This creates a key in .pem format:
#	provisioner "file" {
#		source      = ".\\terraform.exe"
#		destination = "${local.home_dir}/terraform.exe"
#		
#		connection {
#			type        = "ssh"
#			user        = "ubuntu"
#			private_key = file("${local.key_file}.pem")
#			host        = "${self.public_dns}"
#		}
#	}
#------------------------------------------------------------------------------------------------

locals {

	#------------------------------------------------------------------------------------------------
	# INSTANCE TYPE
	#------------------------------------------------------------------------------------------------
	ec2 = "i3.xlarge" # 4vcpu 2core 30ram 950ssd <= 10gb
	dsk = 900         # size in GB to use
	#------------------------------------------------------------------------------------------------
	#ec2 = "t3.xlarge"
	#ec2 = "c5d.2xlarge" # 8cpu 16ram ssd <= 10gb
	#------------------------------------------------------------------------------------------------
	jvm  = "-Djava.net.preferIPv4Stack=true -Xms1g -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:+DisableExplicitGC"
	#------------------------------------------------------------------------------------------------

	#------------------------------------------------------------------------------------------------
	# VIRGINIA
	#------------------------------------------------------------------------------------------------
	base_region = "us-east-1"
	base_zone   = "us-east-1a"
	ami_image   = "ami-0557a15b87f6559cf"
	key_name    = "fns_keypair"
	key_file    = "fns_keypair"
	#------------------------------------------------------------------------------------------------
	
	#------------------------------------------------------------------------------------------------
	# FRANCOFORTE
	#------------------------------------------------------------------------------------------------
	#base_region = "eu-central-1"
	#base_zone   = "eu-central-1a"
	#ami_image   = "ami-0d1ddd83282187d18"
	#key_name    = "fns_keypair_francoforte"
	#key_file    = "fns_keypair_francoforte"
	#------------------------------------------------------------------------------------------------

	#------------------------------------------------------------------------------------------------
	# OHIO
	#------------------------------------------------------------------------------------------------
	#base_region = "us-east-2"
	#base_zone   = "us-east-2a"
	#ami_image   = "??"
	#key_name    = "??"
	#key_file    = "??"
	#------------------------------------------------------------------------------------------------

	#------------------------------------------------------------------------------------------------
	# OREGON
	#------------------------------------------------------------------------------------------------
	#base_region = "us-west-2"
	#base_zone   = "us-west-2a"
	#ami_image   = "??"
	#key_name    = "??"
	#key_file    = "??"
	#------------------------------------------------------------------------------------------------
		
	#------------------------------------------------------------------------------------------------
	access_key  = "PLEASE PUT YOUR OWN"
	secret_key  = "PLEASE PUT YOUR OWN"
	bucket_out  = "fns-bucket-results"
	bucket_code = "fns-bucket-code"
	bucket_find = "fns-bucket-discovery"
	code_zip    = "fns.zip"
	username    = "ubuntu"
	home_dir    = "/home/ubuntu"
	work_dir    = "/home/ubuntu/fns"
	results_dir = "/home/ubuntu/fns/doc_experiments/"	
	#------------------------------------------------------------------------------------------------
}

#------------------------------------------------------------------------------------------------

variable "workers" {
	type        = number
	description = "Number of worker to create for simulation"
}

#------------------------------------------------------------------------------------------------

provider "aws" {
	access_key = "${local.access_key}"
	secret_key = "${local.secret_key}"
	region	   = "${local.base_region}"
}

###############################################################
#
# VPC + NETWORK CONFIGURATION
#
###############################################################

resource "aws_vpc" "fns_vpc" {
	cidr_block = "10.0.0.0/16"
  
	enable_dns_hostnames = true
	enable_dns_support   = true
	
	tags = {
		Name = "fns-vpc"
	}	
}

#------------------------------------------------------------------------------------------------

resource "aws_route_table" "fns_route_table" {
	vpc_id = "${aws_vpc.fns_vpc.id}"

	route {
		cidr_block = "0.0.0.0/0"
		gateway_id = "${aws_internet_gateway.fns_gateway.id}"
	}

	tags = {
		Name = "fns-route-table"
	}	
}

#------------------------------------------------------------------------------------------------

resource "aws_route_table_association" "fns_subnet_association" {
	subnet_id      = "${aws_subnet.fns_subnet.id}"
	route_table_id = "${aws_route_table.fns_route_table.id}"
}

#------------------------------------------------------------------------------------------------

resource "aws_internet_gateway" "fns_gateway" {
	vpc_id = "${aws_vpc.fns_vpc.id}"
	
	tags = {
		Name = "fns-gateway"
	}
}

resource "aws_subnet" "fns_subnet" {
	vpc_id            = "${aws_vpc.fns_vpc.id}"
	cidr_block        = "${cidrsubnet(aws_vpc.fns_vpc.cidr_block, 3, 1)}"
	availability_zone = "${local.base_zone}"
	
	tags = {
		Name = "fns-subnet"
	}	
}

#------------------------------------------------------------------------------------------------

resource "aws_security_group" "fns_sg" {
	name = "fns-sg-allow-all"

	vpc_id = "${aws_vpc.fns_vpc.id}"
	
	# RULE EXAMPLE, SSH 
	#ingress {
	#	cidr_blocks = ["0.0.0.0/0"]
	#	from_port   = 22
	#	to_port     = 22
	#	protocol    = "tcp"
	#}
	
	ingress {
		cidr_blocks = ["0.0.0.0/0"]
		from_port   = 0
		to_port     = 0
		protocol    = "-1"
	}
	
	egress {
		cidr_blocks = ["0.0.0.0/0"]
		from_port   = 0
		to_port     = 0
		protocol    = "-1"
	}
}

######################################################################
#
# KINESIS DATASTREAM (fns_kinesis_stream) <- FIREHOSE (fns_kinesis_firehose) -> S3 (fns_bucket_out)
#
######################################################################

#------------------------------------------------------------------------------------------------
# STREAM
#------------------------------------------------------------------------------------------------

resource "aws_kinesis_stream" "fns_kinesis_stream" {
	name             = "fns-stream"
	retention_period = 24

	stream_mode_details {
		stream_mode = "ON_DEMAND"
	}
}

#------------------------------------------------------------------------------------------------
# FIREHOSE
#------------------------------------------------------------------------------------------------

resource "aws_kinesis_firehose_delivery_stream" "fns_kinesis_firehose" {
	name        = "fns-kinesis-firehose"
	destination = "extended_s3"

	kinesis_source_configuration {
		kinesis_stream_arn = "${aws_kinesis_stream.fns_kinesis_stream.arn}"
		role_arn           = "${aws_iam_role.fns_kinesis_stream_firehose_role.arn}"
	}
	
	extended_s3_configuration {
		role_arn           = "${aws_iam_role.fns_kinesis_firehose_s3_role.arn}"
		bucket_arn         = "${aws_s3_bucket.fns_bucket_out.arn}"
		compression_format = "UNCOMPRESSED"
		buffer_size        = 128
		buffer_interval    = 60 # MINIMUM VALUE - EXPRESSED IN SECONDS
	}

	#------------------------------------------------------------------------------------------------
	# Create before firehose:
	#	1) aws_s3_bucket - S3 Bucket
	#	2) fns_kinesis_stream - Kinesis STREAM
	#------------------------------------------------------------------------------------------------
	
	depends_on = [aws_kinesis_stream.fns_kinesis_stream,aws_s3_bucket.fns_bucket_out]
}

#------------------------------------------------------------------------------------------------
# FIREHOSE WRITE TO S3
#------------------------------------------------------------------------------------------------

resource "aws_iam_role" "fns_kinesis_firehose_s3_role" {
	name = "fns-kinesis-firehose-s3-role"
	assume_role_policy = jsonencode({
		Version = "2012-10-17"
		Statement = [{
			Effect = "Allow"
			Principal = {
				Service = "firehose.amazonaws.com"
			}
			Action = "sts:AssumeRole"
			Sid    = ""
		}]
	})
}

resource "aws_iam_policy" "fns_kinesis_firehose_s3_policy" {
	name    = "fns-kinesis-firehose-s3-policy"
	policy  = jsonencode({
		Version = "2012-10-17"
		Statement = [{
            "Sid": "",
            "Effect": "Allow"
            "Action": [
                "s3:*",
                "kinesis:*"
            ]
            "Resource": "*"
		}]
	})
}

resource "aws_iam_role_policy_attachment" "fns_kinesis_firehose_s3_policy_attachment" {
	policy_arn = "${aws_iam_policy.fns_kinesis_firehose_s3_policy.arn}"
	role       = "${aws_iam_role.fns_kinesis_firehose_s3_role.name}"
}

#------------------------------------------------------------------------------------------------
# FIREHOSE READ FROM STREAM
#------------------------------------------------------------------------------------------------

resource "aws_iam_role" "fns_kinesis_stream_firehose_role" {
	name = "fns-kinesis-stream-firehose-role"
	
	assume_role_policy = jsonencode({
		Version = "2012-10-17"
		Statement = [{
			Effect = "Allow"
			Principal = {
				Service = "firehose.amazonaws.com"
			}
			Action = "sts:AssumeRole"
		}]
	})
}

resource "aws_iam_policy" "fns_kinesis_stream_firehose_policy" {
	name = "fns-kinesis-stream-firehose-policy"

	policy = jsonencode({
		Version = "2012-10-17"
		Statement = [{
			Effect = "Allow"
			Action = [
				"kinesis:*"
			]
			Resource = aws_kinesis_stream.fns_kinesis_stream.arn
		}]
	})
}

resource "aws_iam_role_policy_attachment" "fns_kinesis_stream_firehose_policy_attachment" {
	policy_arn = aws_iam_policy.fns_kinesis_stream_firehose_policy.arn
	role       = aws_iam_role.fns_kinesis_stream_firehose_role.name
}

######################################################################
#
# S3 CREATE + CODE UPLOAD + IGNITE DISCOVERY
#
######################################################################

resource "aws_s3_bucket" "fns_bucket_out" {
	bucket = "${local.bucket_out}"
	
	force_destroy = true
}

resource "aws_s3_bucket" "fns_bucket_code" {
	bucket = "${local.bucket_code}"
	
	force_destroy = true
}

resource "aws_s3_bucket" "fns_bucket_find" {
	bucket = "${local.bucket_find}"
	
	force_destroy = true
}

resource "aws_s3_object" "fns_bucket_code_uploaded" {
	bucket = "${local.bucket_code}"
	key    = "${local.code_zip}"
	source = "..\\zip\\${local.code_zip}"
	
	depends_on = [aws_s3_bucket.fns_bucket_code]
}

resource "aws_s3_bucket_policy" "fns_public_bucket_code_policy" {
	bucket = "${aws_s3_bucket.fns_bucket_code.id}"

	policy = jsonencode({
		Version = "2012-10-17"
		Statement = [{
			Effect = "Allow"
			Principal = "*"
			Action = [
				"s3:GetObject"
			]
			Resource = [
				"${aws_s3_bucket.fns_bucket_code.arn}",
				"${aws_s3_bucket.fns_bucket_code.arn}/*"
			]
		}]
	})
}

######################################################################
#
# EC2 TO S3 - ACCESS ROLE
#
######################################################################

resource "aws_iam_role" "fns_ec2_s3_role" {
	name = "fns-ec2-s3-role"
	assume_role_policy = jsonencode({
		Version = "2012-10-17"
		Statement = [{
			Effect = "Allow"
			Principal = {
				Service = "ec2.amazonaws.com"
			}
			Action = "sts:AssumeRole"
			Sid    = ""
		}]
	})
}

resource "aws_iam_policy" "fns_ec2_s3_policy" {
	name    = "fns-ec2-s3-policy"
	policy  = jsonencode({
		Version = "2012-10-17"
		Statement = [{
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "s3:*",
                "ec2:*"
            ],
            "Resource": "*"
		}]
	})
}

resource "aws_iam_role_policy_attachment" "fns_ec2_s3_policy_attachment" {
	policy_arn = "${aws_iam_policy.fns_ec2_s3_policy.arn}"
	role       = "${aws_iam_role.fns_ec2_s3_role.name}"
}

resource "aws_iam_instance_profile" "fns_ec2_s3_instance_profile" {
  name = "fns-ec2-s3-instance-profile"
  role = "${aws_iam_role.fns_ec2_s3_role.name}"
}

######################################################################
#
# SERVER WORKER
#
######################################################################

resource "aws_instance" "fns_worker" {
	ami           = "${local.ami_image}"
	instance_type = "${local.ec2}"
	key_name      = "${local.key_name}"
  
	security_groups = ["${aws_security_group.fns_sg.id}"]

	iam_instance_profile = "${aws_iam_instance_profile.fns_ec2_s3_instance_profile.name}"

	root_block_device {
		volume_size = "${local.dsk}"
		volume_type = "gp3"
		
		delete_on_termination = true
	}
	
	count = var.workers

	subnet_id = "${aws_subnet.fns_subnet.id}"
	
	associate_public_ip_address = true

	#------------------------------------------------------------------------------------------------
	# if bucket is in a region different than current region must be specified as :
	# wget https://${aws_s3_bucket.bucket.id}.s3.<region>.amazonaws.com/${var.file_path}
	# s3 address: https://fns-bucket-code.s3.amazonaws.com/fns.zip
	#------------------------------------------------------------------------------------------------

	user_data = <<-EOF
					#!/bin/bash

					sudo apt update
					
					sudo apt install -y unzip
					sudo apt install -y iperf3
					sudo apt install -y openjdk-11-jdk-headless
					sudo sysctl -w vm.swappiness=0
					sudo sysctl -w vm.zone_reclaim_mode=0
					sudo sysctl -w vm.dirty_writeback_centisecs=500
					sudo sysctl -w vm.dirty_expire_centisecs=500
					
					cd ${local.home_dir}
					
					wget https://${aws_s3_object.fns_bucket_code_uploaded.bucket}.s3.amazonaws.com/${aws_s3_object.fns_bucket_code_uploaded.key}
					wget https://dlcdn.apache.org/ignite/2.14.0/apache-ignite-2.14.0-bin.zip

					mkdir ${local.work_dir}
					mkdir ${local.results_dir}

					unzip -d ${local.work_dir} ${local.code_zip}
					unzip apache-ignite-2.14.0-bin.zip
					
					echo "cd ${local.home_dir}/fns ; sleep 30003" > fns_run.sh
					
					echo "sudo chown -R ${local.username}:${local.username} * ; cd ${local.work_dir} ; java ${local.jvm} -cp ./lib:./fns.jar it.ppu.StartWorker -x ${count.index + 1} -a ${local.access_key} -s ${local.secret_key} -r ${local.base_region}" > fns_run.sh
					
					sudo chmod 755 fns_run.sh

					# nohup bash ./fns_run.sh && disown -h $!
					
					nohup ./fns_run.sh &

					echo "DONE" > INSTANCE_WORKER_${count.index + 1}_OK.txt					

					sudo chown -R ${local.username}:${local.username} *					
				EOF

	tags = {
		Name = "WORKER-${count.index + 1}"
	}

	provisioner "local-exec" {
		command = "echo start /B putty.exe ubuntu@${self.public_dns} -i ${local.key_file}.ppk > SSH_run_WORKER-${count.index + 1}.bat"
	}
	
	provisioner "local-exec" {
		command = "echo start /B winscp\\WinSCP.exe /privatekey=${local.key_file}.ppk -hostkey=* sftp://ubuntu@${self.public_dns}${local.results_dir} > FTP_run_WORKER-${count.index + 1}.bat"
	}
	
	depends_on = [aws_s3_object.fns_bucket_code_uploaded,aws_s3_bucket.fns_bucket_find]
}

######################################################################
#
# SERVER MAIN
#
######################################################################

resource "aws_instance" "fns_main" {
	ami           = "${local.ami_image}"
	instance_type = "${local.ec2}"
	key_name      = "${local.key_name}"
  
	security_groups = ["${aws_security_group.fns_sg.id}"]

	iam_instance_profile = "${aws_iam_instance_profile.fns_ec2_s3_instance_profile.name}"

	root_block_device {
		volume_size = "${local.dsk}"
		volume_type = "gp3"
		
		delete_on_termination = true
	}
	
	subnet_id = "${aws_subnet.fns_subnet.id}"
	
	associate_public_ip_address = true

	#------------------------------------------------------------------------------------------------
	# if bucket is in a region different than current region must be specified as :
	# wget https://${aws_s3_bucket.bucket.id}.s3.<region>.amazonaws.com/${var.file_path}
	# s3 address: https://fns-bucket-code.s3.amazonaws.com/fns.zip	
	#------------------------------------------------------------------------------------------------
	# IPERF3 USAGE:
	# AS server: iperf3 -s -V -p 47500
	# AS client: iperf3 -c <private IP> -u -b 1G -t 15 -p 47500 --reverse(or -d bidirectional) --parallel 15
	#------------------------------------------------------------------------------------------------

	user_data = <<-EOF
					#!/bin/bash

					sudo apt update
					
					sudo apt install -y unzip
					sudo apt install -y iperf3
					sudo apt install -y openjdk-11-jdk-headless
					sudo sysctl -w vm.swappiness=0
					sudo sysctl -w vm.zone_reclaim_mode=0
					sudo sysctl -w vm.dirty_writeback_centisecs=500
					sudo sysctl -w vm.dirty_expire_centisecs=500
					
					cd ${local.home_dir}
					
					wget https://${aws_s3_object.fns_bucket_code_uploaded.bucket}.s3.amazonaws.com/${aws_s3_object.fns_bucket_code_uploaded.key}
					wget https://dlcdn.apache.org/ignite/2.14.0/apache-ignite-2.14.0-bin.zip

					mkdir ${local.work_dir}
					mkdir ${local.results_dir}

					unzip -d ${local.work_dir} ${local.code_zip}
					unzip apache-ignite-2.14.0-bin.zip
					
					echo "sudo chown -R ${local.username}:${local.username} * ; cd ${local.work_dir} ; java ${local.jvm} -cp ./lib:./fns.jar it.ppu.StartMain   -p _current_ -n -a ${local.access_key} -s ${local.secret_key} -r ${local.base_region}" > fns_run.sh
					echo "sudo chown -R ${local.username}:${local.username} * ; cd ${local.work_dir} ; java ${local.jvm} -cp ./lib:./fns.jar it.ppu.StartStatus                 -a ${local.access_key} -s ${local.secret_key} -r ${local.base_region}" > fns_status.sh
					
					sudo chmod 755 fns_run.sh
					sudo chmod 755 fns_status.sh
					
					echo "DONE" > INSTANCE_MAIN_OK.txt

					sudo chown -R ${local.username}:${local.username} *										
				EOF

	tags = {
		Name  = "MAIN"
	}

	provisioner "local-exec" {
		command = "echo start /B putty.exe ubuntu@${self.public_dns} -i ${local.key_file}.ppk > SSH_run_MAIN.bat"
	}
	
	provisioner "local-exec" {
		command = "echo start /B winscp\\WinSCP.exe /privatekey=${local.key_file}.ppk -hostkey=* sftp://ubuntu@${self.public_dns}${local.results_dir} > FTP_run_MAIN.bat"
	}
	
	depends_on = [aws_s3_object.fns_bucket_code_uploaded,aws_s3_bucket.fns_bucket_find]
}
