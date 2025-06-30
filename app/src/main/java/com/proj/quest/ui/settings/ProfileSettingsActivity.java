package com.proj.quest.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.UpdateProfileRequest;
import com.proj.quest.models.User;
import com.proj.quest.models.AvatarResponse;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.utils.FileUtils;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import android.util.Log;

public class ProfileSettingsActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivAvatar;
    private EditText etLogin, etEmail, etPassword;
    private Button btnSave, btnChangeAvatar, btnBack;
    private Uri avatarUri;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private String oldLogin, oldEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        ivAvatar = findViewById(R.id.ivAvatar);
        etLogin = findViewById(R.id.etLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnBack = findViewById(R.id.btnBack);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        // Отображаем текущий аватар
        String avatarUrl = sharedPrefs.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                .load(avatarUrl.startsWith("http") ? avatarUrl : "http://5.175.92.194:3000" + avatarUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(ivAvatar);
        }

        // Получаем старые значения из Intent
        oldLogin = getIntent().getStringExtra("login");
        oldEmail = getIntent().getStringExtra("email");
        if (oldLogin != null) etLogin.setText(oldLogin);
        if (oldEmail != null) etEmail.setText(oldEmail);

        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfileSettings());
        btnBack.setOnClickListener(v -> finish());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), avatarUri);
                ivAvatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfileSettings() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Нет токена", Toast.LENGTH_SHORT).show();
            return;
        }
        String login = etLogin.getText().toString().isEmpty() ? oldLogin : etLogin.getText().toString();
        String email = etEmail.getText().toString().isEmpty() ? oldEmail : etEmail.getText().toString();
        String password = etPassword.getText().toString().isEmpty() ? null : etPassword.getText().toString();
        UpdateProfileRequest req = new UpdateProfileRequest(login, email, password);
        apiService.updateProfile("Bearer " + token, req).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Если выбран новый аватар — загружаем его
                    if (avatarUri != null) {
                        String mimeType = getContentResolver().getType(avatarUri);
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(avatarUri);
                            byte[] bytes = readBytes(inputStream);
                            RequestBody reqFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "image/*"), bytes);
                            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", "avatar.jpg", reqFile);
                            apiService.uploadAvatar("Bearer " + token, body).enqueue(new Callback<AvatarResponse>() {
                                @Override
                                public void onResponse(Call<AvatarResponse> call, Response<AvatarResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String newAvatarUrl = response.body().getAvatarUrl();
                                        sharedPrefs.saveAvatarUrl(newAvatarUrl);
                                    }
                                    Toast.makeText(ProfileSettingsActivity.this, "Профиль и аватар обновлены", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                @Override
                                public void onFailure(Call<AvatarResponse> call, Throwable t) {
                                    Toast.makeText(ProfileSettingsActivity.this, "Ошибка загрузки аватара", Toast.LENGTH_SHORT).show();
                                    Log.e("AvatarUpload", "Ошибка отправки аватара", t);
                                    finish();
                                }
                            });
                        } catch (IOException e) {
                            Toast.makeText(ProfileSettingsActivity.this, "Ошибка чтения файла", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProfileSettingsActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(ProfileSettingsActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileSettingsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
} 