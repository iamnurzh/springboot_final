package kz.iitu.csse.group34.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="users")
    private Users author;

    @ManyToOne
    @JoinColumn(name = "newsPost")
    private NewsPost newspost;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date")
    private Date date;
}
