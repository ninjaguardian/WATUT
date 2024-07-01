package com.corosus.watut.cloudRendering.threading;

import com.corosus.coroutil.util.CULog;

public class ThreadedCloudBuilderJob extends Thread {

    private ThreadedCloudBuilder threadedCloudBuilder;

    public ThreadedCloudBuilderJob(ThreadedCloudBuilder threadedCloudBuilder) {
        this.threadedCloudBuilder = threadedCloudBuilder;
    }

    @Override
    public void run() {
        CULog.log("cloud render thread start");
        threadedCloudBuilder.doWork();
        threadedCloudBuilder.setWaitingToUploadData(true);
        threadedCloudBuilder.setRunning(false);
        CULog.log("cloud render thread complete");
    }

}
