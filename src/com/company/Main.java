package com.company;

import java.sql.Timestamp;
import java.util.Random;

class DaveTimer {
    public static enum State {
        Stopped,
        Paused,
        Running
    }

    State _state;
    private long _timerStart;

    private long _pauseAccum;
    private long _curPauseStart;

    public DaveTimer() {
        Cancel();
    }

    private long _getCurMilli() {
        return System.currentTimeMillis();
    }

    public State State() {
        return _state;
    }

    /*
	Gives elapsed time since start, not counting any paused time
	 */
    public long GetElapsed() {
        if (_state == State.Running) {
            return (_getCurMilli() - _timerStart) - _pauseAccum;
        } else if (_state == State.Paused) {
            return (_curPauseStart - _timerStart) - _pauseAccum;
        }

        return 0;
    }

    public void Restart() {
        _state = State.Running;
        _timerStart = _getCurMilli();
        _pauseAccum = 0;
        _curPauseStart = 0;
    }

    public void Pause() {
        if (_state == State.Running) {
            _state = State.Paused;
            _curPauseStart = _getCurMilli();
        }
    }

    public void Unpause() {
        if (_state == State.Paused) {
            _pauseAccum += (_getCurMilli() - _curPauseStart);
            _state = State.Running;
            _curPauseStart = 0;
        }
    }

    public void Cancel() {
        _state = State.Stopped;
        _timerStart = _getCurMilli();
        _pauseAccum = 0;
        _curPauseStart = 0;
    }
}




/* Pausable timer class
	Uses System.nanoTime, but also records the start time using System.curTimeMillis so it knows the global time for nanoTime
 */
class DaveTimerPrecise {
    public static enum State {
        Stopped,
        Paused,
        Running
    }

    State _state;
    private long _timerStartMS;     // Used only for converting nano to clock time since nano has an arbitrary start origin
    private long _timerStartNano;

    private long _pauseAccumNano;
    private long _curPauseStartNano;

    public DaveTimerPrecise() {
        Cancel();
    }

    private long _getCurMilli() {
        return System.currentTimeMillis();
    }

    private long _getCurNano() {
        return System.nanoTime();
    }

    private long _nanoToMilli(long nano) {
        return nano / 1000000;
    }
    private long _milliToNano(long milli) {
        return milli * 1000000;
    }
    private long _nanoToLocal( long nano ) {
        return nano - _timerStartNano;
    }

    public long GetStartTime() {
        return _timerStartMS;
    }
    public long GetPausedTime() {
        if (_state == State.Paused)
            return _timerStartMS + _nanoToMilli( _nanoToLocal(_curPauseStartNano) );

        return 0;
    }

    public State State() {
        return _state;
    }

    //Gives elapsed time since start, ignoring any paused time
    public long GetElapsed_nano() {
        if (_state == State.Running)
            return (_getCurNano() - _timerStartNano) - _pauseAccumNano;
        else if (_state == State.Paused)
            return (_curPauseStartNano - _timerStartNano) - _pauseAccumNano;

        return 0;
    }

    public long GetElapsed() {
        return _nanoToMilli(GetElapsed_nano());
    }

    public void Restart() {
        _state = State.Running;
        _timerStartMS = _getCurMilli();
        _timerStartNano = _getCurNano();
        _pauseAccumNano = 0;
        _curPauseStartNano = 0;
    }

    public void Pause() {
        if (_state == State.Running) {
            _state = State.Paused;
            _curPauseStartNano = _getCurNano();
        }
    }

    public void Unpause() {
        if (_state == State.Paused) {
            _state = State.Running;
            _pauseAccumNano += _getCurNano() - _curPauseStartNano;
            _curPauseStartNano = 0;
        }
    }

    public void Cancel() {
        _state = State.Stopped;
        _timerStartMS = _getCurMilli();
        _timerStartNano = _getCurNano();
        _pauseAccumNano = 0;
        _curPauseStartNano = 0;
    }
}

public class Main {

    public static void Sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        DaveTimerPrecise dtimep = new DaveTimerPrecise();
        DaveTimer dtime = new DaveTimer();
        dtime.Restart();
        dtimep.Restart();
        Sleep(1000);
        dtime.Pause();
        dtimep.Pause();
        Sleep(501);
        dtime.Unpause();
        dtimep.Unpause();
        Sleep(1001);
        dtime.Pause();
        dtimep.Pause();
        Sleep(3001);
        System.out.println(dtime.GetElapsed());
        System.out.println(dtimep.GetElapsed_nano());
        System.out.println("dtimep.GetStartTime() = " + dtimep.GetStartTime());
        System.out.println("dtimep.GetPausedTime() = " + dtimep.GetPausedTime());
        Timestamp ts = new Timestamp( dtimep.GetPausedTime());
        System.out.println(new Timestamp(dtimep.GetStartTime()) + ", "+ ts);
    }
}
