package com.popstars.repository;

import com.popstars.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 好友关系表。
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    @Query("select f from Friendship f where f.status = 'ACCEPTED' and (f.userId = :userId or f.friendId = :userId) order by f.id desc")
    List<Friendship> findAccepted(@Param("userId") Long userId);

    List<Friendship> findByFriendIdAndStatusOrderByIdDesc(Long friendId, String status);

    List<Friendship> findByUserIdAndStatusOrderByIdDesc(Long userId, String status);

    void deleteByUserIdOrFriendId(Long userId, Long friendId);
}
