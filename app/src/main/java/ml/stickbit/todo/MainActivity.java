package ml.stickbit.todo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.text.format.DateFormat.is24HourFormat;

public class MainActivity extends AppCompatActivity {
    public MainActivity main;
    public String lastText;
    ToDoItem i;
    List<ToDoItem> reminds = new ArrayList<>();
    public Map<Integer, String> aIDTogID = new HashMap<>();
    public Map<String, TextView> gIDToView = new HashMap<>();
    public static String toLoad = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = this;
        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   Snackbar.make(v.getRootView(),"test",Snackbar.LENGTH_LONG).show();
                i = new ToDoItem();
                getDialogInfo(0);
            }
        });

        try {
            reminds = (List<ToDoItem>) fileToObj("reminds");
            for (ToDoItem i : reminds) {
                addIt(i);
            }
        } catch (Exception e) {
            reminds = new ArrayList<>();
        }
        if (toLoad != null) {
            gIDToView.get(toLoad).callOnClick();
            toLoad = null;
        }
    }

    public void getDialogInfo(int stage) {
        switch (stage) {
            case 0: {
                lastText = null;
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.todoItem);
                final EditText input = new EditText(this);
                input.setMaxLines(1);
                input.setSingleLine(true);
                input.setLines(1);
                builder.setView(input);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastText = input.getText().toString();
                        getDialogInfo(1);
                    }
                });
                builder.setCancelable(false);
                builder.show();
                break;
            }
            case 1: {
                if (lastText == null || lastText.trim().isEmpty()) {
                    getDialogInfo(0);
                    return;
                }

                i.shortText = lastText;

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.descPrompt);
                //  final EditText input = new EditText(this);
                //input.setMaxLines(1);
                // input.setSingleLine(true);
                //  input.setLines(1);
                //  builder.setView(input);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialogInfo(2);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialogInfo(3);
                    }
                });
                builder.setCancelable(false);
                builder.show();
                break;
            }
            case 2: {
                lastText = null;
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.desPromptWord);
                final EditText input = new EditText(this);
                builder.setView(input);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastText = input.getText().toString();
                        if (lastText.trim().isEmpty()) {
                            getDialogInfo(2);
                            return;
                        }
                        i.longText = lastText;
                        getDialogInfo(3);
                    }
                });
                builder.setCancelable(false);
                builder.show();
                break;
            }
            case 3: {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.setDDatePrompt);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialogInfo(4);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialogInfo(5);
                    }
                });
                builder.setCancelable(false);
                builder.show();
                break;
            }
            case 4: {
                final await a = new await();
                long time = System.currentTimeMillis();
                LocalDateTime t = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
                a.t = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        i.hour = hourOfDay;
                        i.minute = minute;
                    }
                }, t.getHour(), t.getMinute(), is24HourFormat(this));
                a.d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        i.year = year;
                        i.month = month;
                        i.day = dayOfMonth;
                    }
                }, t.getYear(), t.getMonthValue(), t.getDayOfMonth());
                a.t.setCancelable(false);
                final TimePickerDialog ti = a.t;
                final DatePickerDialog da = a.d;
                a.t.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        ti.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    }
                });
                a.d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        da.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    }
                });
                a.d.setCancelable(false);
                a.t.show();
                a.d.show();
                a.start();
                break;
            }
            case 5: {
                i.id = UUID.randomUUID().toString();
                reminds.add(i);
                addIt(i);
                objToFile(reminds, "reminds");
            }
        }
    }

    public void addIt(final ToDoItem i) {
        LinearLayout l = findViewById(R.id.linear);
        final View view = View.inflate(main, R.layout.checkboxlayer, null);
        view.setId(View.generateViewId());
        aIDTogID.put(view.getId(), i.id);
        l.addView(view);
        final TextView t = view.findViewWithTag("theTextBox");
        t.setText(i.shortText);
        gIDToView.put(i.id, t);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = ((View) (v.getParent())).getId();
                String guid = aIDTogID.get(id);
                final ToDoItem i = guidToTodo(guid);
                //  Toast.makeText(main, i == null ? "null :(" : "Clicked: " + i.shortText + "\n\n" + i.longText + "\n\n" + i.done, Toast.LENGTH_LONG).show();
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(main);
                View vie = View.inflate(main, R.layout.detailsdialog, null);
                b.setView(vie);

                TextView title = vie.findViewWithTag("titleName");
                title.setText(i.shortText);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(main);
                        final EditText e = new EditText(main);
                        e.setSingleLine(true);
                        e.setMaxLines(1);
                        e.setLines(1);
                        b.setView(e);
                        b.setTitle(R.string.todoItem);
                        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (e.getText() != null && !e.getText().toString().trim().isEmpty()) {
                                    i.shortText = e.getText().toString();
                                    main.recreate();
                                    objToFile(reminds, "reminds");
                                    toLoad = i.id;
                                }
                            }
                        });
                        b.setCancelable(false);
                        b.show();
                    }
                });
                TextView desc = vie.findViewWithTag("longDesc");
                desc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(main);
                        final EditText e = new EditText(main);
                        b.setView(e);
                        b.setTitle(R.string.desPromptWord);
                        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (e.getText() != null && !e.getText().toString().trim().isEmpty()) {
                                    i.longText = e.getText().toString();
                                    main.recreate();
                                    objToFile(reminds, "reminds");
                                    toLoad = i.id;
                                }
                            }
                        });
                        b.setCancelable(false);
                        b.show();
                    }
                });
                desc.setText(i.longText == null ? getString(R.string.noDesc) : i.longText);

                try {
                    String text = getString(R.string.noTime);
                    if (i.hour != -1) {
                        boolean is24 = DateFormat.is24HourFormat(main);
                        java.text.DateFormat inDF = new SimpleDateFormat("HH:mm");
                        java.text.DateFormat oDF = is24 ? new SimpleDateFormat("HH:mm") : new SimpleDateFormat("hh:mm aa");

                        String time = oDF.format(inDF.parse(i.hour + ":" + i.minute));
                        text = getString(R.string.dueAt) + " " + time;
                    }
                    TextView t = vie.findViewWithTag("dueTime");
                    t.setText(text);
                    t.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final TimePickerDialog t = new TimePickerDialog(main, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    i.hour = hourOfDay;
                                    i.minute = minute;
                                    main.recreate();
                                    objToFile(reminds, "reminds");
                                    toLoad = i.id;
                                }
                            }, i.hour, i.minute, is24HourFormat(main));
                            t.setCancelable(false);
                            t.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    t.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                                }
                            });
                            t.show();
                        }
                    });
                    FloatingActionButton bu = vie.findViewWithTag("deleteButton");
                    bu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reminds.remove(i);
                            objToFile(reminds, "reminds");
                            main.recreate();
                        }
                    });
                    CalendarView cal = vie.findViewWithTag("calendarPopup");
                    cal.setMinDate(System.currentTimeMillis());
                    Date d = new SimpleDateFormat("M/d/yyyy").parse(i.month + 1 + "/" + i.day + "/" + i.year);
                    System.out.println("Date:: " + i.month + "/" + i.day + "/" + i.year + "\n\n" + d.getTime());
                    cal.setDate(d.getTime(), false, true);
                    cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                            i.day = dayOfMonth;
                            i.month = month;
                            i.year = year;
                            System.out.println("HEY THE DATE CHANGED!!");
                            objToFile(reminds, "reminds");
                        }
                    });

                } catch (Exception ignored) {
                }
                Dialog d = b.create();
                d.show();
            }
        });
        CheckBox b = view.findViewWithTag("theCheckBox");
        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int id = ((View) (buttonView.getParent())).getId();
                String guid = aIDTogID.get(id);
                ToDoItem i = guidToTodo(guid);
                i.done = isChecked;
                objToFile(reminds, "reminds");
            }
        });
        b.setChecked(i.done);

    }

    public ToDoItem guidToTodo(String guid) {
        for (ToDoItem i : reminds) {
            if (i.id.equals(guid))
                return i;
        }
        return null;
    }


    private class await extends Thread {
        public DatePickerDialog d;
        public TimePickerDialog t;

        @Override
        public void run() {
            while (true) {
                if (!d.isShowing() && !t.isShowing()) {
                    if (i.hour == -1 || i.year == -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                main.getDialogInfo(4);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                main.getDialogInfo(5);
                            }
                        });
                    }
                    return;
                }
            }
        }
    }

    public void objToFile(Object o, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(String.valueOf(Paths.get(main.getDataDir() + "/" + fileName)));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object fileToObj(String fileName) {
        Object o = null;
        try {
            FileInputStream fileIn = new FileInputStream(String.valueOf(Paths.get(main.getDataDir() + "/" + fileName)));
            ObjectInputStream in = new ObjectInputStream(fileIn);
            o = in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception ignored) {
        }
        return o;
    }

}