package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView result_textview, solution_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        result_textview = findViewById(R.id.result_textview);
        solution_textview = findViewById(R.id.solution_textview);

        int[] buttonIds = {
                R.id.buttons_0, R.id.buttons_1, R.id.buttons_2, R.id.buttons_3,
                R.id.buttons_4, R.id.buttons_5, R.id.buttons_6, R.id.buttons_7,
                R.id.buttons_8, R.id.buttons_9, R.id.buttons_dot,
                R.id.buttons_addition, R.id.buttons_subtract,
                R.id.buttons_multiple, R.id.buttons_divide,
                R.id.buttons_openbracket, R.id.buttons_closebracket,
                R.id.buttons_C, R.id.buttons_AC, R.id.buttons_equal
        };

        for (int id : buttonIds) {
            MaterialButton btn = findViewById(id);
            btn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        MaterialButton button = (MaterialButton) v;
        String text = button.getText().toString();
        String exp = solution_textview.getText().toString();

        switch (text) {

            case "AC":
                solution_textview.setText("");
                result_textview.setText("0");
                return;

            case "C":
                if (!exp.isEmpty()) {
                    exp = exp.substring(0, exp.length() - 1);
                }
                break;

            case "=":
                if (exp.isEmpty() || !isValidExpression(exp)) {
                    result_textview.setText("Syntax Error");
                    return;
                }

                String result = getResult(exp);
                result_textview.setText(result);
                solution_textview.setText(result.equals("Syntax Error") ? exp : result);
                return;

            default:
                if (!canAppend(exp, text)) return;
                exp += text;
        }

        solution_textview.setText(exp);

        if (isValidExpression(exp)) {
            String preview = getResult(exp);
            if (!preview.equals("Syntax Error")) {
                result_textview.setText(preview);
            }
        }
    }

    /* ------------------ HELPERS ------------------ */

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    private boolean isValidExpression(String exp) {
        int balance = 0;

        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);

            if (c == '(') balance++;
            if (c == ')') balance--;

            if (balance < 0) return false;

            if (i > 0 && isOperator(c) && isOperator(exp.charAt(i - 1)))
                return false;
        }

        char last = exp.charAt(exp.length() - 1);
        return balance == 0 && !isOperator(last) && last != '(';
    }

    private boolean canAppend(String exp, String text) {
        char curr = text.charAt(0);

        if (exp.isEmpty()) {
            return curr == '(' || Character.isDigit(curr);
        }

        char last = exp.charAt(exp.length() - 1);

        if (isOperator(curr)) {
            return !isOperator(last) && last != '(';
        }

        if (curr == '(') {
            return isOperator(last) || last == '(';
        }

        if (curr == ')') {
            return count(exp, '(') > count(exp, ')') && !isOperator(last);
        }

        return true;
    }

    private int count(String s, char c) {
        int count = 0;
        for (char ch : s.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }

    private String getResult(String exp) {
        try {
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();

            exp = exp.replace("X", "*").replace("÷", "/");

            Object result = context.evaluateString(scriptable, exp, "js", 1, null);
            String ans = result.toString();

            if (ans.equals("Infinity") || ans.equals("NaN")) return "Syntax Error";
            if (ans.endsWith(".0")) ans = ans.replace(".0", "");

            return ans;
        } catch (Exception e) {
            return "Syntax Error";
        } finally {
            Context.exit();
        }
    }
}
