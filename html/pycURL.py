#!/usr/bin/python
#this is a short programm to test the abbility for requesting the wps-Server of the TU-Dresden
#it is not very flexible and is actually written just for one special case
#requirements: 	-data (for wps-request) has to be set in an special file
#			-->name, format & location is defined by the constants 'filename' & 'location'
#			-->has to fit the requirements of OGC-WPS version 1.0.0
#		-works actually just for one polygon geometry
#		-Python has to be installed on the system
#		-pycURL has to be installed on the system 	--> requesting the wps-server
#		-Shapely has to be installed on the system	--> creating polygon geometry
#			--> not ultimatley needed...can be commented (last 4 lines)
#		-the requested server is defined by the constant 'server'
#			--> two servers a predefined...the local- and the TU-Dresden-Server...just uncomment the desired
#Author: Ralf Klammer


#pycURL has to be installed
import pycurl
import time

#Constants
#*********
filename = "execute.xml"
location = "./"
server = "http://localhost:8080/wps-dev/wps"
#server = "http://kartographie.geo.tu-dresden.de/webgen_wps/wps"

#Functions
#*********
#wps() sends the request via pycURL to the server 
def wps(url):	
	#open the datafile
	data = open(location+filename).read()
	#print data
	
	#set pycURL
	c = pycurl.Curl()
	c.setopt(pycurl.URL, url)
	c.setopt(pycurl.HTTPHEADER, ["Content-Type: text/xml"])
	c.setopt(pycurl.POSTFIELDS, data)
	
	import StringIO
	b = StringIO.StringIO()
	c.setopt(pycurl.WRITEFUNCTION, b.write)
	c.setopt(pycurl.FOLLOWLOCATION, 1)
	c.setopt(pycurl.MAXREDIRS, 5)
	c.perform()
	#print b.getvalue()
	response = b.getvalue()

	return response

#find() returns a string that is between a tag (2 strings)
def find(string, before, behind):	
	e = string.partition(before)
	f = e[2].partition(behind)
	found = f[0]
	#print f[0]

	return found
#save() writes any content to a file...!not explecitely needed!
def save(content,locationstore,filestore):	
	FILE = open(locationstore+filestore,"w")
	FILE.write(content)
	FILE.close()

#Main programm
#*********

print 'Request the WPS-Servers via pycURL'

#send the execute command
print server
response = wps(server)
print response

#find the url of status-xml 
found = find(response,'statusLocation="','" xmlns:wps')

status_xml = found
print "Status-xml: ", status_xml

#read the status-xml
response = ""
finished = False
count = 0
while finished is False:
	response = wps(status_xml)
	#print response
	
	found = find(response,'<wps:ProcessAccepted>','</wps:ProcessAccepted>')	
	
	if  count == 0:
		print found
		count = count+1
	if found == "Request is in progress...":
		finished = False
	else:
		finished = True
		found = find(response,'<wps:ProcessSucceeded>','</wps:ProcessSucceeded>')
		if found == "Process finished successfully.":
			print found
		else:
			print "Any problem occured!!!"
	time.sleep(1)



#find the url of resulted xml
found = find(response,'href="','"/>')
result_xml = found	
print "Response-xml: ",result_xml

#read the  resulted xml
response = ""
response = wps(result_xml)
print response

#save result as result.xml
#save(response, location, 'result.xml')






