/**
 * 项目常量定义
 */

// 邮箱正则表达式
export const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@qq\.com$/;

// 密码长度限制
export const PASSWORD_MIN_LENGTH = 6;
export const PASSWORD_MAX_LENGTH = 64;

// 昵称长度限制
export const NICKNAME_MAX_LENGTH = 64;

// 商品标题长度限制
export const GOODS_TITLE_MAX_LENGTH = 120;

// 商品描述长度限制
export const GOODS_DESCRIPTION_MAX_LENGTH = 5000;

// 商品价格最小值
export const GOODS_PRICE_MIN = 0.01;

// 商品图片最大数量
export const GOODS_MAX_IMAGES = 9;

// 会话消息内容最大长度
export const MESSAGE_CONTENT_MAX_LENGTH = 1000;

// 验证码长度
export const VERIFICATION_CODE_LENGTH = 6;

// 验证码冷却时间（秒）
export const CODE_COOLDOWN_SECONDS = 60;

// 商品列表默认分页大小
export const DEFAULT_PAGE_SIZE = 24;

// 会话列表默认分页大小
export const CONVERSATION_PAGE_SIZE = 50;

// 消息列表默认分页大小
export const MESSAGE_PAGE_SIZE = 100;
