package ml.stickbit.todo;

import java.io.Serializable;

public class ToDoItem implements Serializable {
    public String shortText;
    public String longText;
    public int year = -1, month = -1, day = -1, hour = -1, minute = -1;
    public String id;
    public boolean done = false;
}