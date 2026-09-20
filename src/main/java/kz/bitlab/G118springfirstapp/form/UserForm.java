package kz.bitlab.G118springfirstapp.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    @Size(max = 254, message = "Email должен быть не длиннее 254 символов")
    private String email;

    @NotBlank(message = "Введите имя")
    @Size(max = 100, message = "Имя должно быть не длиннее 100 символов")
    private String fullName;

    private Long cityId;
}
