package com.corosus.watut.cloudRendering.threading;

import com.corosus.coroutil.util.CULog;

public class ThreadedCloudBuilderJob extends Thread {

    private ThreadedCloudBuilder threadedCloudBuilder;
    private boolean running = true;

    public ThreadedCloudBuilderJob(ThreadedCloudBuilder threadedCloudBuilder) {
        this.threadedCloudBuilder = threadedCloudBuilder;
    }

    @Override
    public void run() {
        //CULog.log("cloud render thread start");
        while (running) {
            try {
                if (!threadedCloudBuilder.tickThreaded()) {
                    Thread.sleep(200);
                } else {
                    Thread.sleep(200);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        /*threadedCloudBuilder.setWaitingToUploadData(true);
        threadedCloudBuilder.setRunning(false);*/
        //CULog.log("cloud render thread complete");
    }

    public void stopCloudBuilderThread() {
        this.running = false;
    }

}
