package org.vadar;

import com.fazecast.jSerialComm.SerialPort;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Main {

    // https://www.xanthium.in/cross-platform-serial-port-programming-tutorial-java-jdk-arduino-embedded-system-tutorial
    // https://www.raspberrypi.com/documentation/accessories/build-hat.html
    // https://datasheets.raspberrypi.com/build-hat/build-hat-serial-protocol.pdf
    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
        System.out.println("Serial Test:");

        int baudRate = 115200;
        int dataBits = 8;
        int stopBit = SerialPort.ONE_STOP_BIT;
        int parity = SerialPort.NO_PARITY;

        SerialPort arduinoPort = null;
        SerialPort[] AvailablePorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : AvailablePorts) {
            if (serialPort.getDescriptivePortName().contains("Arduino")) {
                arduinoPort = serialPort;
            }
            System.out.println("'" + serialPort.getDescriptivePortName() + "'\t'" + serialPort.getSystemPortName() + "'");
        }

        if (arduinoPort == null) {
            System.err.println("failed to find the Arduino COM port!");
            return;
        }

        arduinoPort.setComPortParameters(baudRate, dataBits, stopBit, parity);
        arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);

        // decent chance the arduino resets when we open the port
        arduinoPort.openPort();
        Thread.sleep(2000);

        writeToArduino(arduinoPort);

        // timings are a pain in the arse in the serial... need to wait & if no bytes read generally means passed the read timeout
//        Thread.sleep(15000);
//        readFromArduino(arduinoPort);

        arduinoPort.closePort();
        System.out.println("done!");
    }

    private static void writeToArduino(SerialPort arduinoPort) {
        //String command = "port 0; pwm; set 1:";
        //String command = "port 0 ; pwm ; set 1\r";
        String command = "M1 OFF" +
                ":";
        byte[] writeBytes = command.getBytes();

        int bytesTxed = arduinoPort.writeBytes(writeBytes, writeBytes.length);
        System.out.println("#bytes Transmitted -> " + bytesTxed);
        System.out.println("command sent: '" + command.replace('\r', ' ') + "'");
    }

    private static void readFromArduino(SerialPort arduinoPort) throws UnsupportedEncodingException {
        System.out.println("Reading from Arduino on serial port");
        if (arduinoPort.isOpen()) {

            byte[] readBuffer = new byte[100];


            int numRead = arduinoPort.readBytes(readBuffer, readBuffer.length);
            if (numRead > 0) {

            // serial from arduino ends with CRLF
            String message = new String(Arrays.copyOfRange(readBuffer, 0, numRead), "UTF-8").replace("\r\n", "");
            System.out.println("reply: #bytes: " + numRead + ", message: '" + message + "'");
            }
        } else {
            System.out.println("Read failed, Arduino Port is not open");
        }
    }
}