/***************************************************************************
Simple Programm to test the usage of cURL in C!
Performs a simple HTTP-GET Request for the WPS-Operations GetCapabilities and DescribeProcess
Usage: 	1st compile - e.g. with  'g++ simple_cURL.c -o simple_cURL -lcurl'
		&
   	than execute with './simple_cURL'
Author: Ralf Klammer
Source: http://curl.haxx.se/libcurl/c/simple.html
 ***************************************************************************/
#include <stdio.h>
#include <curl/curl.h>

int main(void)
{
  CURL *curl;
  CURLcode res;
  char url[] = "http://kartographie.geo.tu-dresden.de/webgen_wps/wps?service=WPS&Request=GetCapabilities";
  //char url[] = "http://kartographie.geo.tu-dresden.de/webgen_wps/wps?service=WPS&Request=DescribeProcess&Service=WPS&Version=1.0.0&Identifier=ch.unizh.geo.webgen.service.LineSmoothing";

  curl = curl_easy_init();
  if(curl) {
    curl_easy_setopt(curl, CURLOPT_URL, url);
    res = curl_easy_perform(curl);
    if(res != CURLE_OK)
      fprintf(stderr, "curl_easy_perform() failed: %s\n",
              curl_easy_strerror(res));

    curl_easy_cleanup(curl);
  }
  return 0;
}
