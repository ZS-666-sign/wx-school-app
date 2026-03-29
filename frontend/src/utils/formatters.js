/**
 * 格式化价格显示
 * @param {number|string} value - 价格值
 * @param {number} decimals - 小数位数，默认0
 * @returns {string} 格式化后的价格字符串
 */
export function formatPrice(value, decimals = 0) {
  const num = Number(value || 0);
  return `¥${num.toFixed(decimals)}`;
}

/**
 * 格式化时间显示
 * @param {string|Date} value - 时间值
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

/**
 * 格式化完整日期时间
 * @param {string|Date} value - 时间值
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(value) {
  if (!value) return "";
  const date = new Date(value);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

/**
 * 格式化相对时间（多久以前）
 * @param {string|Date} value - 时间值
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(value) {
  if (!value) return "";
  const date = new Date(value);
  const now = new Date();
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffSec < 60) return "刚刚";
  if (diffMin < 60) return `${diffMin}分钟前`;
  if (diffHour < 24) return `${diffHour}小时前`;
  if (diffDay < 7) return `${diffDay}天前`;
  return formatTime(value);
}

/**
 * 商品状态文本
 * @param {string} status - 状态码
 * @returns {string} 状态文本
 */
export function statusText(status) {
  const statusMap = {
    ON_SALE: "在售中",
    OFF_SHELF: "已下架",
    SOLD: "已售出"
  };
  return statusMap[status] || status;
}

/**
 * 商品成色文本
 * @param {string} condition - 成色等级
 * @returns {string} 成色文本
 */
export function conditionText(condition) {
  if (!condition) return "成色未知";
  return condition;
}

/**
 * 截断文本
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @returns {string} 截断后的文本
 */
export function truncateText(text, maxLength = 50) {
  if (!text || text.length <= maxLength) return text;
  return text.slice(0, maxLength) + "...";
}
