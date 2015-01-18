package com.antoniovm.lowtency.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.antoniovm.lowtency.util.MathUtils;
import com.antoniovm.util.raw.BlockingQueue;

/**
 * This class handles the audio stream and sends it to the output device
 *
 * @author Antonio Vicente Martin
 */
public class AudioOutputManager extends AudioIOManger implements Runnable {

    private AudioTrack audioTrack;
    private byte[] samplesWritingBuffer;
    private BlockingQueue blockingQueue;

    /**
     * Builds a new AudioOutputManager
     * @param chunkSizeInSamples The base block size in bytes
     */
    public AudioOutputManager(int chunkSizeInSamples) {
        this(AudioIOManger.DEFAULT_SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, chunkSizeInSamples);
    }

    /**
     * Builds a new AudioOutputManager
     * @param sampleRate The samplerate in samples per second
     * @param channelFormat The AudioFormat.CHANNEL_CONFIGURATION_XXXX configuration
     * @param encodingFormat The AudioFormat.ENCODING_XXX configuration
     * @param chunkSizeInSamples The base block size in bytes
     */
    public AudioOutputManager(int sampleRate, int channelFormat, int encodingFormat, int chunkSizeInSamples) {
        int minimumBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelFormat, encodingFormat);
        minimumBufferSize = MathUtils.getUpperClosestMultiple(minimumBufferSize, chunkSizeInSamples * getBytesPerSample(encodingFormat));

        this.samplesWritingBuffer = new byte[minimumBufferSize];
        this.blockingQueue = new BlockingQueue(minimumBufferSize, BlockingQueue.PushPolicy.OVERWRITE_OLD_DATA,minimumBufferSize);
        this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelFormat, encodingFormat,
                minimumBufferSize, AudioTrack.MODE_STREAM);

    }

    /**
     * Writes the samples to the audio device for playback
     * @param samples The raw bytes
     */
    public void writeSamples(byte[] samples) {
        writeSamples(samples, 0, samples.length);
    }

    /**
     * Writes the samples to the audio device for playback
     * @param samples The raw bytes
     * @param i The intial index
     * @param j The final index
     */
    public void writeSamples(byte[] samples, int i, int j) {
        audioTrack.write(samples, i, j);
    }

    /**
     * Requests the audio device to start the playback
     */
    public void play() {
        audioTrack.play();
    }

    /**
     * Requests the audio device to stop the playback
     */
    public void stop() {
        audioTrack.stop();
    }

    /**
     * Returns the number of bytes per sample
     *
     * @return The number of bytes per sample
     */
    public int getBytesPerSample() {
        return getBytesPerSample(audioTrack.getAudioFormat());
    }

    /**
     * Returns the number of bytes per sample
     *
     * @param encodingFormat The encoding type
     * @return The number of bytes per sample
     */
    private static int getBytesPerSample(int encodingFormat) {
        switch (encodingFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                return 1;
            case AudioFormat.ENCODING_PCM_16BIT:
                return 2;
            default:
                break;
        }

        return 0;
    }

    /**
     * @return The buffer length
     */
    public int getBufferLength() {
        return samplesWritingBuffer.length;
    }

    /**
     * Main thread to get the latest data available
     */
    @Override
    public void run() {
        while(true){
            blockingQueue.pop(samplesWritingBuffer);
            writeSamples(samplesWritingBuffer);
        }
    }

    /**
     * Gets the BlockingQueue buffer
     * @return The BlockingQueue
     */
    public BlockingQueue getBlockingQueue() {
        return blockingQueue;
    }
}
