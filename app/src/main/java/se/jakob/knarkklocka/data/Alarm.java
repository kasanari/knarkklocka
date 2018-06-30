package se.jakob.knarkklocka.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "alarm_table")
public class Alarm {

    public static final int STATE_WAITING = 0;
    public static final int STATE_DEAD = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_SNOOZING = 3;

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "alarm_state")
    private int state;
    @NonNull
    @ColumnInfo(name = "start_time")
    private Date startTime;
    @NonNull
    @ColumnInfo(name = "end_time")
    private Date endTime;
    @ColumnInfo(name = "number_of_snoozes")
    private int snoozes;

    @Ignore
    public Alarm(int state, @NonNull Date startTime, @NonNull Date endTime) {
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.snoozes = 0;
    }

    public Alarm(int id, int state, @NonNull Date startTime, @NonNull Date endTime) {
        this.id = id;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.snoozes = 0;
    }

    public int getState() {return state;}

    public void setState(int state) {this.state = state;}

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    @NonNull
    public Date getStartTime() {return startTime;}

    public void setStartTime(@NonNull Date startTime) {this.startTime = startTime;}

    @NonNull
    public Date getEndTime() {return endTime;}

    public void setEndTime(@NonNull Date endTime) {this.endTime = endTime;}

    public int getSnoozes() {return snoozes;}

    public void setSnoozes(int snoozes) {this.snoozes = snoozes;}

    public void incrementSnoozes() {
        this.snoozes += 1;
    }

    public String getStateString(int state) {
        switch (state) {
            case Alarm.STATE_ACTIVE:
                return "Active";
            case Alarm.STATE_WAITING:
                return "Waiting";
            case Alarm.STATE_DEAD:
                return "Dead";
            case Alarm.STATE_SNOOZING:
                return "Snoozed";
            default:
                return "Invalid State";
        }
    }
}

