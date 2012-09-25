/*
	Very simple example to request the WebGen-WPS via C++ using cURL
	--> reads a XML-File that contains geographic data and informations about the server
	--> just sends that data to WebGen-WPS
	--> analysis of response is not implemented

	Author: Ralf Klammer
	Source: Example by Todd Papaioannou (http://www.luckyspin.org/?p=28 [January 3rd, 2006])

   1st compile with  'g++ curl_byC.cpp -o curl_byC -lcurl'
	&
   than execute with './curl_byC' 

   main.cpp 

*/

 #include <string>  
 #include <fstream> 
 #include <iostream>  
 #include "curl/curl.h" 

using namespace std;   
     
   // This is the writer call back function used by curl  
   static int writer(char *data, size_t size, size_t nmemb, std::string *buffer)  
   {  
     // What we will return  
     int result = 0;      
     // Is there anything in the buffer?  
     if (buffer != NULL)  
     {  
       // Append the data to the buffer  
       buffer->append(data, size * nmemb);     
       // How much did we write?  
       result = size * nmemb;  
     }      
     return result;  
   }  

int main()
{
// main processes for datafile	
	//read the XML-File & write to a string
	fstream f;	
	f.open("./execute.xml", ios::in);
	string s, datei = "";
	while (!f.eof())          
    		{
        	getline(f, s);        
		datei = datei + s;      

    		}
    	f.close();
	//transform string to char (cause curl doesn't read string)	    
	const char *data;
	data = datei.c_str();
		
// main part where curl is used
	//set up serveradress
	const char *url;
	url = "http://localhost:8080/wps-dev/wps";
	//url = "http://kartographie.geo.tu-dresden.de/webgen_wps/wps";

	//initialise curl
       	CURL *curl;  
       	CURLcode result;
	string buffer;

	// initialise curl handling  
       	curl = curl_easy_init(); 

	// set up all curl-options
	if (curl)  
       		{
		curl_easy_setopt(curl, CURLOPT_URL, url);
		curl_easy_setopt(curl, CURLOPT_HEADER, "Content-Type: text/xml"); 
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data); 
		curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writer);
		curl_easy_setopt(curl, CURLOPT_WRITEDATA, &buffer); 	
		} 
	// perform server request  
        result = curl_easy_perform(curl); 

	// cleanup  
        curl_easy_cleanup(curl);
	
	// print the response to commandline
	cout << buffer << "\n";
}
