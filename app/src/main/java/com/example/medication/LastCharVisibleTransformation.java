package com.example.medication;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class LastCharVisibleTransformation extends PasswordTransformationMethod {

    private static final char DOT = '\u2022';

    @Override
    public CharSequence getTransformation(CharSequence source, View view){
        return new VisibleLast(source);
    }

    private static class VisibleLast implements CharSequence{
        private final CharSequence source;

        VisibleLast(CharSequence source){
            this.source = source;
        }

        @Override
        public int length(){
            return source.length();
        }

        @Override
        public char charAt(int index){
            if(index == source.length() - 1){
                return source.charAt(index);
            }
            return DOT;
        }

        @Override
        public CharSequence subSequence(int start, int end){
            char[] buf = new char[end - start];
            for(int i = start; i < end; i++){
                buf[i-start] = charAt(i);
            }
            return new String(buf);
        }
    }
}
