from http.server import BaseHTTPRequestHandler, HTTPServer
import logging
import subprocess
import json

class S(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self): # GET VOLUME LEVEL FROM COMPUTER
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        self._set_response()
        bashCommand = "pactl list sinks | grep '^[[:space:]]Volume:' |  sed -e 's,.* \([0-9][0-9]*\)%.*,\\1,'"
        proc = subprocess.Popen(['bash', '-c', bashCommand], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        o, e = proc.communicate()
        volumeLevel = o.decode('ascii').split('\n')
        self.wfile.write(volumeLevel[0].encode('utf-8'))
        # 1 -> depends on your output devices(ex. 0-> Built-In // 1->Connected wireless headphone)

    def do_POST(self):# SET VOLUME LEVEL FROM ANDROID APP
        content_length = int(self.headers['Content-Length'])  # <--- Gets the size of data
        post_data = self.rfile.read(content_length)  # <--- Gets the data itself
        logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\nBody:\n%s\n",
                     str(self.path), str(self.headers), post_data.decode('utf-8'))

        self._set_response()
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))
        getInf = json.loads(post_data.decode('utf-8'))
        setVolume(getInf["volume"])


def setVolume(volume):
    #DEFAULT_SINK -> SELECTED DEFAULT DEVICE FROM PULSEAUDIO VOLUME CONTROL
    increaseVolume = ['pactl', 'set-sink-volume', '@DEFAULT_SINK@', (volume + '%')]
    executeShell(increaseVolume)


def executeShell(command):
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    o, e = proc.communicate()
    print('Output: ' + o.decode('ascii'))
    print('Error: ' + e.decode('ascii'))
    print('code: ' + str(proc.returncode))


def run(server_class=HTTPServer, handler_class=S, port=8080):
    logging.basicConfig(level=logging.INFO)
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    logging.info('Stopping httpd...\n')


if __name__ == '__main__':
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
