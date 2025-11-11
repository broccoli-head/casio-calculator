package com.example.calculator;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final int MAX_INPUT_LENGTH = 8;
    private boolean signProvided = false;
    private boolean pointProvided = false;
    private boolean turnedOn = true;

    //for detecting double click on mrc button
    private long lastClickedTime = 0;
    private long mrcClickedTime = 0;

    private TextView numberInput, signInput;
    private final List<String> operationList = new ArrayList<>();
    private double memory = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        numberInput = findViewById(R.id.numberDisplay);
        signInput = findViewById(R.id.signDisplay);
        TextView memoryDisplay = findViewById(R.id.memoryDisplay);

        List<Button> numericButtons = List.of(
            findViewById(R.id.oneButton),
            findViewById(R.id.twoButton),
            findViewById(R.id.threeButton),
            findViewById(R.id.fourButton),
            findViewById(R.id.fiveButton),
            findViewById(R.id.sixButton),
            findViewById(R.id.sevenButton),
            findViewById(R.id.eightButton),
            findViewById(R.id.nineButton),
            findViewById(R.id.zeroButton),
            findViewById(R.id.pointButton),
            findViewById(R.id.mrcButton)
        );

        List<Button> signButtons = List.of(
            findViewById(R.id.plusButton),
            findViewById(R.id.minusButton),
            findViewById(R.id.multiplyButton),
            findViewById(R.id.divideButton)
        );

        numericButtons.forEach(button -> button.setOnClickListener(view -> {
            if (!turnedOn) return;
            String currentText = numberInput.getText().toString();
            String currentSign = signInput.getText().toString();

            if (signProvided) {
                operationList.add(currentText);

                if (currentSign.equals("×"))
                    operationList.add("*");
                else if (currentSign.equals("÷"))
                    operationList.add("/");
                else
                    operationList.add(currentSign);

                currentText = "0";
                signInput.setText("");
                signProvided = false;
                pointProvided = false;
            }

            String number = button.getText().toString();

            if (number.equals("MRC")) {
                lastClickedTime = mrcClickedTime;
                mrcClickedTime = System.currentTimeMillis();
                String memoryVal = String.valueOf(memory);

                //single click - recall function
                if (mrcClickedTime - lastClickedTime > 250) {
                    if(memoryVal.endsWith(".0"))
                        memoryVal = memoryVal.substring(0, memoryVal.length() - 2);
                    numberInput.setText(memoryVal);
                }
                //double click - clear function
                else memory = 0;
                return;
            }

            if (number.equals("•")) {
                if (!pointProvided && currentText.length() < MAX_INPUT_LENGTH) {
                    String newText = currentText + ".";
                    numberInput.setText(newText);
                    pointProvided = true;
                    return;
                }
                else return;
            }
            if (currentText.length() >= MAX_INPUT_LENGTH) return;

            String newText = currentText.equals("0") ? number : currentText.concat(number);
            numberInput.setText(newText);
        }));


        signButtons.forEach(button -> button.setOnClickListener(view -> {
            if (!turnedOn) return;
            String sign = button.getText().toString();
            signInput.setText(sign);
            signProvided = true;
        }));


        Button executeButton = findViewById(R.id.executeButton);
        Button clearButton = findViewById(R.id.clearButton);
        Button clearAllButton = findViewById(R.id.clearAllButton);
        Button squareRootButton = findViewById(R.id.squareRootButton);
        Button percentButton = findViewById(R.id.percentButton);
        Button offButton = findViewById(R.id.offButton);
        Button memoryAdd = findViewById(R.id.plusMButton);
        Button memoryRemove = findViewById(R.id.minusMButton);


        executeButton.setOnClickListener(view -> {
            if (!turnedOn) return;
            String currentText = numberInput.getText().toString();
            operationList.add(currentText);

            List<String> rpn = RPN.convertEquation(operationList);
            double result = RPN.evaluate(rpn);

            Log.d("FIRST", operationList.toString());
            Log.d("SECOND", rpn.toString());

            String outputValue = String.valueOf(result);
            if (outputValue.length() > MAX_INPUT_LENGTH)
                outputValue = outputValue.substring(0, 8);

            if(outputValue.endsWith(".0"))
                outputValue = outputValue.substring(0, outputValue.length() - 2);

            boolean dotFound = false;
            for (char digit : outputValue.toCharArray()) {
                if(digit == '.') {
                    dotFound = true;
                    break;
                }
            }

            numberInput.setText(outputValue);
            signInput.setText("=");
            operationList.clear();
            pointProvided = dotFound;
        });


        clearButton.setOnClickListener(view -> {
            if (!turnedOn) return;
            reset();
        });
        clearAllButton.setOnClickListener(view -> {
            if (!turnedOn) return;
            reset();
        });


        squareRootButton.setOnClickListener(view -> {
            if (!turnedOn) return;
            String currentText = numberInput.getText().toString();
            double value = Double.parseDouble(currentText);
            value = Math.sqrt(value);

            String outputValue = String.valueOf(value);
            if (outputValue.length() > MAX_INPUT_LENGTH)
                outputValue = outputValue.substring(0, 8);
            numberInput.setText(outputValue);
        });

        percentButton.setOnClickListener(view -> {
            if (!turnedOn) return;
            String currentText = numberInput.getText().toString();
            double value = Double.parseDouble(currentText);
            value *= 0.01;

            if(String.valueOf(value).length() <= MAX_INPUT_LENGTH)
                numberInput.setText(String.valueOf(value));
        });


        offButton.setOnClickListener(view -> {
            if (turnedOn) {
                reset();
                memory = 0;
                offButton.setText("ON");
                numberInput.setText("");
                turnedOn = false;
            }
            else {
                offButton.setText("OFF");
                numberInput.setText("0");
                turnedOn = true;
            }
        });

        memoryAdd.setOnClickListener(view -> {
            if (!turnedOn) return;
            memory += Double.parseDouble(numberInput.getText().toString());
        });
        memoryRemove.setOnClickListener(view -> {
            if (!turnedOn) return;
            memory -= Double.parseDouble(numberInput.getText().toString());
        });
    }

    private void reset() {
        numberInput.setText("0");
        signInput.setText("");
        operationList.clear();
        pointProvided = false;
        signProvided = false;
    }
}
