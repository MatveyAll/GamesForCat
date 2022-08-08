
package com.example.gamesforcat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

public class CatTranslatorActivity extends AppCompatActivity {

    ListView soundsView;
    TextView catSaid; // поле для вывода "перевода" кошачьих слов
    ImageView recordImage; // поле для красного кружка, который своим миганием показывает, что запись включена
    TextView recordBtn, // подпись на кнопке для начала/конца "записи" (она меняется)
            recordTimer;
    TextView noteView; // примечание внизу экрана

    ArrayList<Sound> sounds = new ArrayList<>(); // почему не обычный массив? а мне так захотелось;
    // вдруг потом добавлю возможность добавлять новые готовые фразы или что-то ещё
    TreeMap<String, int[]> previousPhrases = new TreeMap<>(); // если пользователь введёт одну и ту же фразу, бллагодаря этому ассоциативному массиву,
    // он получит один и тот же "перевод" на кошачий =)
    // (разумеется, пока не выйдет из активности)
    Record record = new Record();
    String[] catPhrases = { // фразы, которые может вывести "переводчик" с кошачьего языка
            "Что тебе надо?", "Отстань.", "Отстань от меня.", "Убери эту штуку.", "Что ты делаешь?",
            "Хватит ерундой маяться.", "Не издевайся надо мной.", "Ты надоел с этой штуковиной.", "Уйди от меня.", "Ты кошачьих слов не понимаешь, что ли?",
            "Да что ты пристал ко мне?", "Не раздражай меня!", "Пожалуйста, прекрати.", "Что творится в головах у человеков...", "Что я тебе сделал-то?"
    };
    String note = "ПРИМЕЧАНИЕ: правильный перевод ни с кошачьего языка, ни на кошачий язык не гарантирован";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cat_translator);

        soundsView = findViewById(R.id.cat_sounds_LV);
        recordBtn = findViewById(R.id.record_TV);
        recordBtn.setText("НАЧАТЬ ЗАПИСЬ");
        catSaid = findViewById(R.id.cat_said_TV);
        recordTimer = findViewById(R.id.record_timer_TV);
        recordImage = findViewById(R.id.record_IV);
        noteView = findViewById(R.id.note_TV);
        noteView.setText(note);

        sounds.add(new Sound("ПРИВЕТ", MediaPlayer.create(this, R.raw.meow_2)));
        sounds.add(new Sound("ДАВАЙ ПОИГРАЕМ", MediaPlayer.create(this, R.raw.meow_1)));
        sounds.add(new Sound("ТЫ МОЛОДЕЦ", MediaPlayer.create(this, R.raw.meow_3)));
        sounds.add(new Sound("ЕДА ГОТОВА", MediaPlayer.create(this, R.raw.meow_4)));
        sounds.add(new Sound("МНЕ ГРУСТНО", MediaPlayer.create(this, R.raw.meow_6)));
        sounds.add(new Sound("МНЕ ПОРА ИДТИ", MediaPlayer.create(this, R.raw.meow_7)));
        sounds.add(new Sound("СЕДУЙ ЗА МНОЙ", MediaPlayer.create(this, R.raw.meow_5)));
        sounds.add(new Sound("Я ЗОЛ НА ТЕБЯ", MediaPlayer.create(this, R.raw.meow_8)));

        soundsView.setAdapter(new Sound.Adapter(this, sounds));

        // готовые фразы вносятся в список уже введённых фраз, но сначала они конвертируются
        String[] convertedSoundNames = new String[sounds.size()];
        Arrays.fill(convertedSoundNames, "");
        for (int i = 0; i < sounds.size(); i++) {
            for (int j = 0; j < sounds.get(i).name.length(); j++) {
                if (Character.isLetter(sounds.get(i).name.charAt(j)))
                    convertedSoundNames[i] += sounds.get(i).name.toLowerCase(Locale.ROOT).charAt(j);
            }
        }
        for (int i = 0; i < convertedSoundNames.length; i++)
            previousPhrases.put(convertedSoundNames[i], new int[]{i});
    }

    public void sayPhrase(View view) {
        EditText userPhrase = findViewById(R.id.enter_phrase_ET);
        String phrase = userPhrase.getText().toString().toLowerCase(Locale.ROOT);
        userPhrase.setText("");
        if (phrase.equals("")) return;

        // если пользователь ввёл ту же фразу, что есть в списке уже введённых фраз, выводится тот же звук,
        // но сначала убираются все лишние символы во фразе
        String convertedPhrase = "";
        for (int i = 0; i < phrase.length(); i++) {
            if (Character.isLetter(phrase.charAt(i)))
                convertedPhrase += phrase.charAt(i);
        }
        phrase = convertedPhrase;

        int meows = phrase.length() / 10;
        if (meows == 0) meows++;
        int[] rands = new int[meows];
        int i = 0;
        for (; i < meows; i++) {
            Sound sound;
            if (previousPhrases.containsKey(phrase))
                sound = sounds.get(Objects.requireNonNull(previousPhrases.get(phrase))[i]); // не знаю, почему, но IDE жалуется,
                // что эта строчка кода может вызывать NullPointerException
            else {
                int rand = (int) (Math.random() * sounds.size());
                sound = sounds.get(rand);
                rands[i] = rand;
            }
            sound.resource.start();
            // делаем перерыв между мяуканиями
            try {
                Thread.sleep(sound.resource.getDuration() + 500);
            } catch (InterruptedException e) {
                catSaid.setText(e.toString());
            }
        }

        if (!previousPhrases.containsKey(phrase)) previousPhrases.put(phrase, rands);
    }

    public void startOrStopRecord(View view) {
        record.isOn = !record.isOn;
        if (record.isOn) {
            record.timer.start();
            recordBtn.setText("ОСТАНОВИТЬ ЗАПИСЬ");
            noteView.setText("");
        } else {
            record.timer.onFinish();
            noteView.setText(note);
        }
    }

    static class Sound {

        String name;
        MediaPlayer resource;

        public Sound(String name, MediaPlayer resource) {
            this.resource = resource;
            this.name = name;
        }

        static class Adapter extends ArrayAdapter<Sound> {

            public Adapter(Context context, ArrayList<Sound> sounds) {
                super(context, R.layout.cat_sound_item, sounds);
            }

            @SuppressLint("InflateParams")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.cat_sound_item, null);

                ((TextView) convertView.findViewById(R.id.cat_sound_TV)).setText(getItem(position).name);
                (convertView.findViewById(R.id.cat_sound_IB)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getItem(position).resource.start();
                    }
                });

                return convertView;
            }
        }
    }

    class Record {

        CountDownTimer timer;

        int timeCount;
        boolean isOn, isImageOn;

        public Record() {
            timer = new CountDownTimer(1000 * 60 * 60, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String out = timeCount / 60 + ":";
                    if (timeCount % 60 <= 9) out += "0" + timeCount % 60;
                    else out += "" + timeCount % 60;
                    recordTimer.setText(out);

                    isImageOn = !isImageOn;
                    if (isImageOn) recordImage.setImageResource(R.drawable.record_circle);
                    else recordImage.setImageResource(0);

                    timeCount++;
                }

                @Override
                public void onFinish() {
                    String out = "";
                    if (timeCount < 2) timeCount = 2;
                    for (int i = 0; i < timeCount / 2; i++)
                        out += catPhrases[(int) (Math.random() * catPhrases.length)] + "\n. . .\n";

                    catSaid.setText(new StringBuffer(out).delete(out.length() - 6, out.length() - 1));

                    recordTimer.setText("");
                    recordImage.setImageResource(0);
                    recordBtn.setText("НАЧАТЬ ЗАПИСЬ");
                    isOn = false;
                    isImageOn = false;
                    timeCount = 0;
                    cancel();
                }
            };
        }
    }
}