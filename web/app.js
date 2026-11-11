import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js';
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js';
import { getFirestore, collection, onSnapshot } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-firestore.js';
import { firebaseConfig } from './firebase-config.js';
const $ = id => document.getElementById(id), message = $('message');
const auth = getAuth(initializeApp(firebaseConfig)), db = getFirestore();
let unsubscribe;
const loginErrors = {
  'auth/invalid-credential': 'Email hoặc mật khẩu chưa đúng. Kiểm tra tài khoản trong Firebase Authentication.',
  'auth/wrong-password': 'Email hoặc mật khẩu chưa đúng. Kiểm tra tài khoản trong Firebase Authentication.',
  'auth/user-not-found': 'Email hoặc mật khẩu chưa đúng. Kiểm tra tài khoản trong Firebase Authentication.',
  'auth/invalid-email': 'Địa chỉ email không hợp lệ.',
  'auth/user-disabled': 'Tài khoản này đã bị vô hiệu hóa trong Firebase Authentication.',
  'auth/operation-not-allowed': 'Phương thức Email/Password chưa được bật trong Firebase Authentication.',
  'auth/too-many-requests': 'Đã thử quá nhiều lần. Vui lòng đợi rồi thử lại.',
  'auth/network-request-failed': 'Không kết nối được Firebase. Kiểm tra kết nối mạng.',
  'auth/api-key-not-valid': 'Cấu hình Firebase Web chưa đúng. Kiểm tra web/firebase-config.js.',
  'auth/invalid-api-key': 'Cấu hình Firebase Web chưa đúng. Kiểm tra web/firebase-config.js.'
};
$('loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  message.textContent = '';
  if (Object.values(firebaseConfig).some(value => !value || value === 'REPLACE_ME')) {
    message.textContent = 'Chưa điền cấu hình Firebase Web trong web/firebase-config.js.';
    return;
  }
  try {
    await signInWithEmailAndPassword(auth, $('email').value.trim(), $('password').value);
  } catch (error) {
    const code = typeof error.code === 'string' && /^auth\/[a-z0-9-]+$/.test(error.code) ? error.code : 'không xác định';
    message.textContent = `${loginErrors[code] || 'Không thể đăng nhập. Kiểm tra cấu hình Firebase và kết nối.'} (Mã lỗi: ${code})`;
  }
});
$('logout').onclick = () => signOut(auth);
let orders = [], authVersion = 0;
const statuses = { pending: 'Chờ nhận', accepted: 'Đang giao', delivered: 'Đã giao' };
const money = n => `${Number(n || 0).toLocaleString('vi-VN')} ₫`;
const dateOf = o => o.createdAt?.toDate?.() || null;
const element = (tag, text, className) => { const el = document.createElement(tag); if (text !== undefined) el.textContent = text; if (className) el.className = className; return el; };
function renderSummary() {
  const delivered = orders.filter(o => o.status === 'delivered');
  const cards = [
    ['Tổng đơn hàng', orders.length.toLocaleString('vi-VN'), 'Tất cả đơn đã ghi nhận', '▤'],
    ['Chờ nhận', orders.filter(o => o.status === 'pending').length.toLocaleString('vi-VN'), 'Cần shipper tiếp nhận', '◷'],
    ['Đang giao', orders.filter(o => o.status === 'accepted').length.toLocaleString('vi-VN'), 'Đơn đang trên đường', '↗'],
    ['Giá trị đã giao', money(delivered.reduce((sum, o) => sum + (Number(o.total) || 0), 0)), 'Tổng giá trị đơn đã giao', '₫']
  ];
  $('metrics').replaceChildren();
  cards.forEach(([title, value, description, icon], i) => {
    const card = element('article', undefined, 'metric');
    const top = element('div', undefined, 'metric-top'); top.append(element('span', title), element('span', icon, `metric-icon icon-${i}`));
    card.append(top, element('strong', value), element('small', description)); $('metrics').append(card);
  });
  const counts = Object.fromEntries(Object.keys(statuses).map(key => [key, orders.filter(o => o.status === key).length]));
  $('statusSummary').replaceChildren();
  Object.entries(statuses).forEach(([key, label]) => {
    const row = element('div', undefined, 'status-row'); const heading = element('div', undefined, 'status-heading');
    heading.append(element('span', label), element('strong', `${counts[key]} đơn`));
    const track = element('div', undefined, 'progress-track'); const fill = element('div', undefined, `progress-fill ${key}`); fill.style.width = `${orders.length ? counts[key] / orders.length * 100 : 0}%`; track.append(fill); row.append(heading, track); $('statusSummary').append(row);
  });
  const unknown = orders.length - Object.values(counts).reduce((a, b) => a + b, 0);
  if (unknown) $('statusSummary').append(element('p', `${unknown} đơn có trạng thái khác.`, 'unknown-status'));
  const today = new Date(); today.setHours(0,0,0,0); const days = [];
  for (let i = 6; i >= 0; i--) { const date = new Date(today); date.setDate(today.getDate() - i); const next = new Date(date); next.setDate(date.getDate()+1); days.push({ date, count: orders.filter(o => { const d = dateOf(o); return d && d >= date && d < next; }).length }); }
  const max = Math.max(1, ...days.map(day => day.count)); $('chart').replaceChildren();
  days.forEach(day => { const column = element('div', undefined, 'chart-column'); const barArea = element('div', undefined, 'bar-area'); const bar = element('div', undefined, 'bar'); bar.style.height = `${Math.max(day.count ? 4 : 1, day.count / max * 100)}%`; bar.title = `${day.count} đơn`; barArea.append(bar); column.append(element('strong', String(day.count), 'bar-value'), barArea, element('span', `${String(day.date.getDate()).padStart(2,'0')}/${String(day.date.getMonth()+1).padStart(2,'0')}`, 'bar-label')); $('chart').append(column); });
}
function renderOrders() {
  const term = $('search').value.trim().toLocaleLowerCase('vi'); const filter = $('statusFilter').value;
  const filtered = orders.filter(o => (filter === 'all' || o.status === filter) && [o.id, o.recipientName, o.phone, o.address, o.items?.[0]?.storeName].some(v => String(v || '').toLocaleLowerCase('vi').includes(term)));
  $('orders').replaceChildren(); $('count').textContent = `Hiển thị ${filtered.length} / ${orders.length} đơn hàng`; $('empty').hidden = filtered.length > 0;
  filtered.forEach(o => {
    const tr = element('tr');
    const orderCell = element('td'); orderCell.append(element('strong', `#${o.id.slice(0, 8)}`), element('small', o.items?.[0]?.storeName || 'Chưa có cửa hàng'));
    const recipient = element('td'); recipient.append(element('strong', o.recipientName || 'Chưa có tên'), element('small', o.phone || 'Chưa có số điện thoại'));
    const date = dateOf(o); const dateCell = element('td', date ? date.toLocaleString('vi-VN', {day:'2-digit',month:'2-digit',year:'numeric',hour:'2-digit',minute:'2-digit'}) : 'Chưa có thời gian');
    const total = element('td', money(o.total), 'amount');
    const state = element('td'); state.append(element('span', statuses[o.status] || 'Khác', `order-status ${o.status === 'pending' || o.status === 'accepted' || o.status === 'delivered' ? o.status : 'other'}`));
    const detailCell = element('td'); const detail = element('button', 'Chi tiết', 'detail-button'); detail.type = 'button'; detail.setAttribute('aria-expanded', 'false'); detailCell.append(detail);
    tr.append(orderCell, recipient, dateCell, total, state, detailCell); $('orders').append(tr);
    const detailRow = element('tr', undefined, 'detail-row'); detailRow.hidden = true; const detailContent = element('td'); detailContent.colSpan = 6;
    const box = element('div', undefined, 'detail-content'); box.append(element('strong', `Mã đơn: ${o.id}`), element('p', `Địa chỉ: ${o.address || 'Chưa có địa chỉ'}`), element('p', `Người nhận: ${o.recipientName || 'Chưa có thông tin'} · ${o.phone || 'Chưa có số điện thoại'}`));
    const items = element('ul'); (Array.isArray(o.items) ? o.items : []).forEach(item => items.append(element('li', `${item.name || 'Món ăn'} × ${item.quantity ?? 0} · ${money(item.price)} / món`))); if (!items.children.length) items.append(element('li', 'Chưa có thông tin món')); box.append(items); detailContent.append(box); detailRow.append(detailContent); $('orders').append(detailRow);
    detail.onclick = () => { detailRow.hidden = !detailRow.hidden; detail.setAttribute('aria-expanded', String(!detailRow.hidden)); detail.textContent = detailRow.hidden ? 'Chi tiết' : 'Thu gọn'; };
  });
}
$('search').addEventListener('input', renderOrders); $('statusFilter').addEventListener('change', renderOrders);
onAuthStateChanged(auth, async user => {
  const version = ++authVersion;
  if (unsubscribe) unsubscribe(); unsubscribe = null; orders = [];
  $('dashboard').hidden = true; $('login').hidden = true; $('logout').hidden = !user;
  if (!user) { $('login').hidden = false; return; }
  try {
    const token = await user.getIdTokenResult(true); if (version !== authVersion) return;
    if (token.claims.admin !== true) { message.textContent = 'Tài khoản này chưa được cấp quyền quản trị.'; await signOut(auth); return; }
    message.textContent = ''; $('dashboard').hidden = false; renderSummary(); renderOrders(); $('count').textContent = 'Đang tải dữ liệu...';
    unsubscribe = onSnapshot(collection(db, 'orders'), snapshot => {
      orders = snapshot.docs.map(doc => ({ ...doc.data(), id: doc.id })).sort((a,b) => (dateOf(b)?.getTime() || 0) - (dateOf(a)?.getTime() || 0));
      message.textContent = ''; renderSummary(); renderOrders();
    }, () => { message.textContent = 'Không tải được đơn hàng. Kiểm tra quyền truy cập Firestore.'; });
  } catch { if (version === authVersion) { message.textContent = 'Không thể kiểm tra quyền quản trị.'; $('login').hidden = false; } }
});
