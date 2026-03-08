package com.smartsplit.group;

import jakarta.persistence.*;
import com.smartsplit.user.User;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "group_members")
@Getter
@Setter
public class GroupMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String role;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}