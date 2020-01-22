from http.server import BaseHTTPRequestHandler, HTTPServer
import logging
import subprocess
import json

sinkIndex = 0
class S(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):  # GET VOLUME LEVEL FROM COMPUTER
        global sinkIndex
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        self._set_response()
        o1 = executeShell("pactl list sinks | grep '^[[:space:]]Volume:' |  sed -e 's,.* \([0-9][0-9]*\)%.*,\\1,'")
        volumeLevel = o1.split('\n')

        o2 = executeShell("pacmd list-sinks | grep '[[:space:]]index:' |  sed -e 's,.* \([0-9][0-9]*\)%.*,\\1,'")
        sinks = o2.split('\n')
        sinksIndex = []
        sinkIndex = 0
        j = 0
        for i in sinks:
            sinksIndex.append(i[-1:])
            sinksIndex.append(volumeLevel[j])
            if i.find("*") != -1:
                sinkIndex = sinks.index(i)  # GET LEVEL OF DEFAULT SINK
                sinksIndex[j*2] += "*"
            j += 1
        sinksIndex.remove(sinksIndex.pop())
        self.wfile.write(','.join([str(elem) for elem in sinksIndex]) .encode('utf-8'))
        #self.wfile.write(volumeLevel[sinkIndex].encode('utf-8'))

    def do_POST(self):  # SET VOLUME LEVEL FROM ANDROID APP
        content_length = int(self.headers['Content-Length'])  # <--- Gets the size of data
        post_data = self.rfile.read(content_length)  # <--- Gets the data itself
        logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\nBody:\n%s\n",
                     str(self.path), str(self.headers), post_data.decode('utf-8'))

        self._set_response()
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))
        getInf = json.loads(post_data.decode('utf-8'))
        if str(self.path) == "/change":
            setDefSink(getInf["id"])
        else:
            setVolume(getInf["volume"])


def setVolume(volume):
    # DEFAULT_SINK -> SELECTED DEFAULT DEVICE FROM PULSEAUDIO VOLUME CONTROL
    increaseVolume = "pactl set-sink-volume @DEFAULT_SINK@ " + volume + "%"
    executeShell(increaseVolume)

def setDefSink(id):
    changeSink = "pacmd set-default-sink "+id
    executeShell(changeSink)

def executeShell(command):
    proc = subprocess.Popen(['bash', '-c', command], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    o, e = proc.communicate()
    return o.decode('ascii')


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
