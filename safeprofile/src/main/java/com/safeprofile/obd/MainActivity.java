package com.safeprofile.obd;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends Activity {

    TextView response;
    EditText editTextAddress, editTextPort;
    Button buttonClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonClear = (Button) findViewById(R.id.clearButton);
        response = (TextView) findViewById(R.id.responseTextView);

        ImageButton ibbtnRpm = (ImageButton) findViewById(R.id.btnRpm);
        ibbtnRpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "01 0C","rpm");
                myClient.execute();

            }
        });

        ImageButton ibbtnSpeed = (ImageButton) findViewById(R.id.btnSpeed);
        ibbtnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "01 0D","speed");
                myClient.execute();

            }
        });

        ImageButton ibbtnFuel = (ImageButton) findViewById(R.id.btnFuel);
        ibbtnFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "01 2F","fuel");
                myClient.execute();

            }
        });
/*
        ImageButton ibbtnEngineTemp = (ImageButton) findViewById(R.id.btnEngineTemp);
        ibbtnEngineTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "01 05","engineTemp");
                myClient.execute();

            }
        });

        ImageButton ibbtnEngineWater = (ImageButton) findViewById(R.id.btnEngineWater);
        ibbtnEngineWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "01 67","engineWaterTemp");
                myClient.execute();

            }
        });
*/
        ImageButton ibbtnVolts = (ImageButton) findViewById(R.id.btnVolts);
        ibbtnVolts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), "AT RV","volts");
                myClient.execute();

            }
        });

        buttonClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                response.setText("");
            }
        });
    }

    public class Client extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String strresult = "";
        String strCommandExecute;
        String strtypecommand;


        Client(String addr, int port, String cmd, String typecommand) {
            dstAddress = addr;
            dstPort = port;
            strCommandExecute = cmd;
            strtypecommand = typecommand;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            Long lngDecimal;

            try {

                InetAddress serverAddr = InetAddress.getByName(dstAddress);
                socket = new Socket(serverAddr, dstPort);

                OutputStream outputStream = socket.getOutputStream();

                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                outputStream.write((strCommandExecute + "\r").getBytes());
                outputStream.flush();

                byte b = 0;
                StringBuilder res = new StringBuilder();

                // read until '>' arrives OR end of stream reached
                int a = 0;
                char c = '\0';
                byte[] bb;
                bb = new byte[100];

                // -1 if the end of the stream is reached
                while (((b = (byte) inputStream.read()) > -1)) {
                    bb[a] = b;
                    c = (char) b;
                    if (c == '>') // read until '>' arrives
                    {
                        break;
                    }
                    res.append(c);
                    a++;
                }

                strresult = new String(bb, "ASCII");

                strresult = strresult.replaceAll("\\s", "");
                strresult = strresult.replaceAll("[^a-zA-Z0-9]", "").replaceAll("\\s+", "");
                strresult = res.toString().replaceAll(strCommandExecute, "");
                strresult = strresult.replaceAll("\\s", "");
                strresult = strresult.replaceAll(">", "");

                //strresult = Long.toString(FormatCommandResult(strresult, strtypecommand));
                strresult = FormatCommandResult(strresult, strtypecommand);


            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        public String FormatCommandResult(String ResultCommand, String typecommand){

            String strResultCommand = ResultCommand;
            String strtypecommand = typecommand;
            Long lngResult=null;
            String strResult="";
            float percentage;

            if (strtypecommand == "volts") {
                strResult = ResultCommand.substring(0,strResultCommand.length());
            } else if (strtypecommand == "rpm") {
                lngResult = Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16); //8
                lngResult = lngResult / 4;
                strResult = "RPM " + lngResult.toString();
            } else if (strtypecommand == "engineTemp") {
                lngResult = Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16); //6
                strResult = "Temp. Motor " + lngResult.toString() + (char) 0x00B0;
            } else if (strtypecommand == "fuel") {
                lngResult = 100 * Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16) / 255;
                strResult = lngResult.toString() + " %";
            } else if (strtypecommand == "speed") {
                lngResult = Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16); //6
                strResult = lngResult.toString() + " Km/h";
            } else if (strtypecommand == "accel") {
                lngResult = 100 * Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16) / 255;
                strResult = lngResult.toString() + " %";
            } else if (strtypecommand == "engineOilTemp") {
                lngResult = Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16); //6
                strResult = "Temp. Motor " + lngResult.toString() + (char) 0x00B0;
            } else if (strtypecommand == "engineWaterTemp") {
                lngResult = Long.parseLong(ResultCommand.substring(4,strResultCommand.length()), 16); //6
                strResult = "Temp. Motor " + lngResult.toString() + (char) 0x00B0;
            } else {
                strResult = "nothing";
            }
            return strResult;
        }

	@Override
	protected void onPostExecute(Void result) {
		TextView txtresultView = (TextView) findViewById(R.id.responseTextView);
		txtresultView.setText(strresult);
	}
}}
