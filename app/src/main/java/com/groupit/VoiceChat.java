package com.groupit;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

import groupitapi.groupit.com.Main;

public class VoiceChat {

    Context con;
    public byte[] buffer;
    public static DatagramSocket socket;
    AudioRecord recorder;

    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;

    public VoiceChat(Context con) {
        this.con = con;
    }

    public void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    socket = new DatagramSocket();

                    byte[] buffer = new byte[minBufSize];
                    byte[] receiveData = new byte[4096];

                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(new Main().getIP());

                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    recorder.startRecording();


                    while(status == true) {
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        packet = new DatagramPacket (buffer, buffer.length, destination, new Main().getPort());

                        socket.send(packet);

                        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                                receiveData.length);

                        socket.receive(receivePacket);

                        playMp3(receivePacket.getData());

                    }


                } catch(SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        streamThread.start();
    }

    private void playMp3(final byte[] mp3SoundByteArray) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String ID = UUID.randomUUID().toString();

                    File tempMp3 = File.createTempFile(ID, "mp3", con.getCacheDir());
                    tempMp3.deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(tempMp3);
                    fos.write(mp3SoundByteArray);
                    fos.close();

                    MediaPlayer mp = new MediaPlayer();
                    mp.setDataSource(tempMp3.getPath());
                    mp.prepare();

                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });

                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.reset();
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
}
}
