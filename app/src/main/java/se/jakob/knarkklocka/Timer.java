package se.jakob.knarkklocka;


import static se.jakob.knarkklocka.Timer.State.EXPIRED;
import static se.jakob.knarkklocka.Timer.State.PAUSED;
import static se.jakob.knarkklocka.Timer.State.RUNNING;

public class Timer {

    public enum State {
        RUNNING(1), PAUSED(2), EXPIRED(3);

        /** The value assigned to this State in prior releases. */
        private final int mValue;

        State(int value) {
            mValue = value;
        }

        /**
         * @return the numeric value assigned to this state
         */
        public int getValue() {
            return mValue;
        }

        /**
         * @return the state corresponding to the given {@code value}
         */
        public static State fromValue(int value) {
            for (State state : values()) {
                if (state.getValue() == value) {
                    return state;
                }
            }

            return null;
        }
    }

    /** Unique id for the timer */
    private final int mId;

    /**The duration of the timer*/
    private final long mLength;

    /** The time at which the timer was started */
    private final long mStartTime;

    /** The current state of the timer. */
    private State mState;

    /** The time at which the timer is scheduled to expire */
    private final long mEndTime;

    Timer(int id, State state, long length, long startTime) {
        mId = id;
        mState = state;
        mLength = length;
        mStartTime = startTime;
        mEndTime = startTime + length;
    }

    public long getLength() { return mLength; }

    public int getId() { return mId; }
    public State getState() { return mState; }

    public boolean isRunning() { return mState == RUNNING; }
    public boolean isPaused() { return mState == PAUSED; }
    public boolean isExpired() { return mState == EXPIRED; }

    public void start() {
        if(isRunning()) {
            return;
        }
        mState = RUNNING;
    }

    public void pause() {
        if(isRunning()) {
            return;
        }
        mState = PAUSED;
    }

    public void expire() {
        if(isRunning()) {
            return;
        }
        mState = EXPIRED;
    }



}
