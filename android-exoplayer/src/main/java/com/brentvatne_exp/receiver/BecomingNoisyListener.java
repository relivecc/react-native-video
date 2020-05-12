package com.brentvatne_exp.receiver;

public interface BecomingNoisyListener {

    BecomingNoisyListener NO_OP = new BecomingNoisyListener() {
        @Override public void onAudioBecomingNoisy() {
            // NO_OP
        }
    };

    void onAudioBecomingNoisy();

}
