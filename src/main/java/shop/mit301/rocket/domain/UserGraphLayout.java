package shop.mit301.rocket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_graph_layout")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGraphLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "drag_id", nullable = false)
    private String dragId;

    @Column(name = "pos_left", nullable = false)
    private int posLeft;

    @Column(name = "pos_top", nullable = false)
    private int posTop;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;
}