package kz.bitlab.G118springfirstapp.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String programmingLanguage;
    private City city;
}
