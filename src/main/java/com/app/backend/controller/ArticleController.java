package com.app.backend.controller;

import com.app.backend.dto.PagedArticleDTO;
import com.app.backend.entity.ArticleImage;
import com.app.backend.enums.FilePathEnum;
import com.app.backend.service.ArticleImageService;
import com.app.backend.strategy.context.UploadStrategyContext;
import com.app.backend.vo.ArticleVO;
import com.app.backend.vo.PagedArticleVO;
import com.app.backend.vo.ResultVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.app.backend.entity.Article;
import com.app.backend.exception.UnauthorizedException;
import com.app.backend.service.ArticleService;
import com.app.backend.service.UserService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private ArticleImageService articleImageService;
    
    /**
     * 从请求中获取用户ID
     * @param username 从 JWT token 中解析出的用户名
     * @return 用户ID
     */
    private Integer getCurrentUserId(String username) {
        Integer userId = userService.getUserIdByUsername(username);
        if (userId == null) {
            throw new UnauthorizedException("用户不存在");
        }
        return userId;
    }
    
    /**
     * 创建博文
     */
    @PostMapping("/create")
    public Map<String, Object> createArticle(@RequestBody ArticleVO articleVO,
                                           @RequestAttribute("username") String username) {
        // 从 JWT token 中解析出的 username 获取真实的用户ID
//        Integer authorId = getCurrentUserId(username);

        
        int articleId = articleService.createArticle(articleVO.getTitle(), articleVO.getName(), articleVO.getLatitude(), articleVO.getLongitude(),
                articleVO.getType(), articleVO.getDescription(), articleVO.getTips(), username,articleVO.getAddress(),articleVO.getNotice(),articleVO.getTools());

        int idx=1;
        List<ArticleImage> articleImagesToSave=new ArrayList<>();
        for (String url : articleVO.getAccessUrl()){
            ArticleImage articleImage =new ArticleImage();
            articleImage.setArticleId(articleId);
            articleImage.setImageUrl(url);
            articleImage.setOrderIndex(idx);
            articleImagesToSave.add(articleImage);
        }

        articleImageService.saveBatch(articleImagesToSave);

        Map<String, Object> response = new HashMap<>();
        if (articleId!=-1) {
            response.put("message", "博文创建成功");
            response.put("success", true);
        } else {
            response.put("message", "博文创建失败，请检查输入参数");
            response.put("success", false);
        }
        
        return response;
    }
    
    /**
     * 根据类型查询博文列表
     */
    @GetMapping("/type/{type}")
    public Map<String, Object> getArticlesByType(@PathVariable String type) {
        List<Article> articles = articleService.getArticlesByType(type);
        
        Map<String, Object> response = new HashMap<>();
        if (articles != null) {
            response.put("articles", articles);
            response.put("success", true);
        } else {
            response.put("message", "无效的博文类型");
            response.put("success", false);
        }
        
        return response;
    }
    
    /**
     * 分页查询博文（按类型）
     */
    @PostMapping("/page")
    public IPage<PagedArticleDTO> getArticlesPaged(@RequestBody PagedArticleVO pagedArticleVO,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        IPage<PagedArticleDTO> articlePage = articleService.getArticlesPaged(pagedArticleVO);
        
//        Map<String, Object> response = new HashMap<>();
//        if (articlePage != null) {
//            response.put("articles", articlePage.getRecords());
//            response.put("total", articlePage.getTotal());
//            response.put("pages", articlePage.getPages());
//            response.put("current", articlePage.getCurrent());
//            response.put("size", articlePage.getSize());
//            response.put("success", true);
//        } else {
//            response.put("message", "无效的博文类型");
//            response.put("success", false);
//        }
        
        return articlePage;
    }
    
    /**
     * 获取我的博文列表
     */
    @GetMapping("/my")
    public Map<String, Object> getMyArticles(@RequestAttribute("username") String username,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        // 从 JWT token 中解析出的 username 获取真实的用户ID
        Integer authorId = getCurrentUserId(username);
        
        IPage<Article> articlePage = articleService.getArticlesByAuthorPaged(page, size, authorId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getRecords());
        response.put("total", articlePage.getTotal());
        response.put("pages", articlePage.getPages());
        response.put("current", articlePage.getCurrent());
        response.put("size", articlePage.getSize());
        response.put("success", true);
        
        return response;
    }
    
    /**
     * 根据地理位置范围查询博文
     */
    @GetMapping("/location")
    public Map<String, Object> getArticlesByLocation(@RequestParam Double minLat,
                                                    @RequestParam Double maxLat,
                                                    @RequestParam Double minLng,
                                                    @RequestParam Double maxLng) {
        List<Article> articles = articleService.getArticlesByLocationRange(minLat, maxLat, minLng, maxLng);
        
        Map<String, Object> response = new HashMap<>();
        if (articles != null) {
            response.put("articles", articles);
            response.put("success", true);
        } else {
            response.put("message", "无效的地理位置参数");
            response.put("success", false);
        }
        
        return response;
    }
    
    /**
     * 更新博文
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateArticle(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> request,
                                           @RequestAttribute("username") String username) {
        String title = (String) request.get("title");
        String name = (String) request.get("name");
        BigDecimal latitude = request.get("latitude") != null ? 
                              new BigDecimal(request.get("latitude").toString()) : null;
        BigDecimal longitude = request.get("longitude") != null ? 
                               new BigDecimal(request.get("longitude").toString()) : null;
        String type = (String) request.get("type");
        String description = (String) request.get("description");
        String tips = (String) request.get("tips");
        
        boolean success = articleService.updateArticle(id, title, name, latitude, longitude, 
                                                      type, description, tips);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("message", "博文更新成功");
            response.put("success", true);
        } else {
            response.put("message", "博文更新失败");
            response.put("success", false);
        }
        
        return response;
    }
    
    /**
     * 删除博文
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteArticle(@PathVariable Integer id,
                                            @RequestAttribute("username") String username) {
        // 从 JWT token 中解析出的 username 获取真实的用户ID
        Integer authorId = getCurrentUserId(username);
        
        boolean success = articleService.deleteArticle(id, authorId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("message", "博文删除成功");
            response.put("success", true);
        } else {
            response.put("message", "博文删除失败，可能不存在或无权限");
            response.put("success", false);
        }
        
        return response;
    }
    
    /**
     * 获取博文详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getArticleById(@PathVariable Integer id) {
        Article article = articleService.getById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (article != null) {
            response.put("article", article);
            response.put("success", true);
        } else {
            response.put("message", "博文不存在");
            response.put("success", false);
        }
        
        return response;
    }


    @PostMapping("/images")
    public ResultVO<String> saveArticleImages(MultipartFile file) {
        return ResultVO.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.ARTICLE.getPath()));
    }


    @PostMapping("/like")
    public ResultVO<String> dealLike(){
        return ResultVO.ok();
    }
}