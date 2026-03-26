package com.example.medication;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medication.util.SprefsManager;

public class MyInfoActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etNickname, etPassword;
    private Button btnSaveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        ivBack = findViewById(R.id.iv_back);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        btnSaveInfo = findViewById(R.id.btn_save_info);

        // 기존에 저장된 닉네임을 불러와서 미리 채워둡니다.
        String currentNickname = SprefsManager.getUserNickName(this);
        etNickname.setText(currentNickname);

        // 뒤로가기 버튼 클릭 이벤트
        ivBack.setOnClickListener(v -> finish());

        // 수정하기 버튼 클릭 이벤트
        btnSaveInfo.setOnClickListener(v -> {
            String newNickname = etNickname.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            if (newNickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "새로운 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 차후에 이 부분에서 백엔드 API (Retrofit)로 수정 요청을 보내야 합니다.
            // 일단은 성공했다고 가정하고 Toast 메시지를 띄운 뒤 화면을 닫습니다.
            Toast.makeText(this, "정보가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 빈 공간 터치 시 키보드 내리기
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            View v = getCurrentFocus();
            if( v instanceof EditText){
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())){
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}