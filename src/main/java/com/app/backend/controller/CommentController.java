package com.app.backend.controller;

import com.app.backend.entity.Comment;
import com.app.backend.service.ArticleService;
import com.app.backend.service.CommentService;
import com.app.backend.service.UserService;
import com.app.backend.vo.CommentVO;
import com.app.backend.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 添加评论
     * @param commentVO 评论信息
     * @return 评论结果
     */
    @PostMapping("/add")
    public ResultVO<String> addComment(@RequestBody CommentVO commentVO, @RequestAttribute("username") String username) {
        try {
            // 参数校验
            if (commentVO.getArticleId() == null) {
                return ResultVO.fail("博文ID不能为空");
            }
            
            if (commentVO.getCommentContent() == null || commentVO.getCommentContent().trim().isEmpty()) {
                return ResultVO.fail("评论内容不能为空");
            }

            if (commentVO.getUserId() == null) {
                return ResultVO.fail("用户不存在");
            }

            
            // 验证博文是否存在
            if (articleService.getById(commentVO.getArticleId()) == null) {
                return ResultVO.fail("评论的博文不存在");
            }
            
            // 创建评论对象
            Comment comment = new Comment();
            comment.setUserId(commentVO.getUserId());
            comment.setArticleId(commentVO.getArticleId());
            comment.setCommentContent(commentVO.getCommentContent().trim());
            comment.setIsDelete(0); // 未删除
            comment.setUserName(username);
            comment.setCreateTime(LocalDateTime.now());
            comment.setUpdateTime(LocalDateTime.now());
            
            // 保存评论
            boolean success = commentService.save(comment);
            if (success) {
                return ResultVO.ok("评论添加成功");
            } else {
                return ResultVO.fail("评论添加失败");
            }
        } catch (Exception e) {
            return ResultVO.fail("评论添加失败: " + e.getMessage());
        }
    }
}