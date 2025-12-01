## 现状与模型

* 用户域模型来自`mall-model`：`Employee`（用户名`Employee.java:20`/密码`Employee.java:22`）、`Role`、`Permission`（权限标识`Permission.java:20`为`perms`）、关联`EmployeeRole`与`RolePermission`。

* `mall-security`尚未实现安全配置；Mapper集中在`mall-service/mall-user`。

## 目标

* 在`mall-security`实现统一的认证、授权、异常处理、密码加密。

* 引入Redis管理登录态：登录后将用户会话写入Redis；每次请求通过Redis确认是否已登录。

* 权限以`Permission.perms`装配为`GrantedAuthority`，支持URL与方法级授权。

## 依赖与配置

* 依赖：`spring-boot-starter-security`、`spring-boot-starter-data-redis`、JWT（`jjwt-*`或`oauth2-resource-server`）。

* Redis：使用`Lettuce`（默认），提供`RedisTemplate<String, Object>`或`StringRedisTemplate`；通过`application.yml`配置`spring.data.redis.host/port/password`。

## 认证流程（含Redis登录态）

1. 登录接口（由`mall-user`实现）：

   * 根据`username`查询`Employee`与权限集合（经`Employee → Role → Permission`）。

   * 验证密码（BCrypt）。

   * 生成JWT（载荷含`uid/username/authorities/exp`）。

   * 写入Redis登录态：

     * `auth:token:{token}` → `AuthSession`（`uid/username/authorities/issuedAt/expiresAt`），TTL=Token过期时间。

     * 可选单点：`auth:user:{uid}` → 当前`token`（用于限制同一用户同时登录数）。

   * 返回`accessToken`与过期时间。
2. 请求鉴权（`JwtAuthenticationFilter`）：

   * 解析`Authorization: Bearer <token>`；校验JWT签名与过期。

   * 访问Redis：若不存在`auth:token:{token}`或TTL已失效→判定未登录，返回`401`。

   * 存在则反序列化`AuthSession`为`Authentication`（`UsernamePasswordAuthenticationToken`，携带`authorities`）并注入`SecurityContext`。
3. 退出登录（由`mall-user`实现）：

   * 删除`auth:token:{token}`；可同步删除`auth:user:{uid}`或维护黑名单。
4. 刷新Token：

   * 校验旧Token有效且存在Redis；签发新Token；原`auth:token:{old}`删除，写入`auth:token:{new}`与重置TTL。

## 授权策略

* `SecurityFilterChain`：关闭CSRF，`STATELESS`会话；放行`/auth/login`、`/auth/refresh`、`/error`、静态资源、`/actuator/**`；其余需认证。

* `@EnableMethodSecurity`：使用`@PreAuthorize("hasAuthority('product:read')")`等；权限字符串来源于`Permission.perms`。

## 异常与统一响应

* `AuthenticationEntryPoint`：未登录或Token无效返回`401`JSON。

* `AccessDeniedHandler`：权限不足返回`403`JSON。

* 记录关键审计日志：登录成功/失败、鉴权失败、退出登录。

## mall-security 模块内容

* `config.SecurityConfig`：`SecurityFilterChain`、`PasswordEncoder`、Method Security启用。

* `jwt.JwtTokenProvider`：签发/解析/校验JWT。

* `jwt.JwtAuthenticationFilter`：从请求读取Token→校验→从Redis载入会话→设置认证上下文。

* `session.RedisTokenStore`：封装Redis读写（key命名、TTL、序列化）；提供`save(token, AuthSession, ttl)`、`get(token)`、`delete(token)`等。

* `handler.RestAccessDeniedHandler`、`RestAuthenticationEntryPoint`：统一异常输出。

* `model.AuthUser`/`model.AuthSession`：认证主体与会话载体（含`uid/username/authorities/exp`）。

* `service.SecurityUserService`接口：按`username`加载`Employee`并组装`Permission.perms`集合；由`mall-user`实现（使用其Mapper）。

* `constants.SecurityConstants`：`AUTH_HEADER`、`TOKEN_PREFIX`、Redis key前缀等。

## 集成步骤

* `mall-user`：

  * 实现`SecurityUserService`，完成员工、角色、权限查询与组装。

  * 提供`/auth/login`、`/auth/logout`、`/auth/refresh`端点并调用`RedisTokenStore`与`JwtTokenProvider`。

* 受保护服务（`mall-product`、`mall-order`等）：

  * 引入`mall-security`依赖并启用配置；无需直连数据库，只依赖Redis校验登录态。

  * 在业务方法上声明所需权限（`@PreAuthorize`）。

* 配置：在各服务`application.yml`设置JWT密钥与Redis连接。

## 测试与验证

* 单测：`JwtTokenProvider`、`RedisTokenStore`（序列化/TTL）、`SecurityConfig`允许路径。

* 集成测试：

  * 插入示例权限（如`product:read`、`product:write`）。

  * 登录→Redis写入→带Token访问受保护接口→验证`200/403/401`。

  * 退出登录→删除Redis→再次访问返回`401`。

* 性能与安全：Token短期有效+刷新；限制并发登录（可选）；Redis键前缀与TTL规范；密钥安全管理。

## 可选增强

* 单点登录：登录时覆盖`auth:user:{uid}`并在Filter中校验当前Token与用户唯一Token一致。

* 黑名单：退出或强制下线将Token加入黑名单键空间，在Filter中二次校验拦截。

