package se.jakob.knarkklocka.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "alarm_table")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "alarm_state")
    private int state;
    @ColumnInfo(name = "start_time")
    private Date startTime;
    @ColumnInfo(name = "end_time")
    private Date endTime;
    @ColumnInfo(name = "number_of_snoozes")
    private int snoozes;

    public Alarm(int id, int state, Date startTime, Date endTime) {
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

    public Date getStartTime() {return startTime;}

    public void setStartTime(Date startTime) {this.startTime = startTime;}

    public Date getEndTime() {return endTime;}

    public void setEndTime(Date endTime) {this.endTime = endTime;}

    public int getSnoozes() {return snoozes;}

    public void setSnoozes(int snoozes) {this.snoozes = snoozes;}
}
