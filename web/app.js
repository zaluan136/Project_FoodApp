import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js';
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js';
import { getFirestore, collection, query, orderBy, onSnapshot } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-firestore.js';
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
onAuthStateChanged(auth, async user => {
  if (unsubscribe) unsubscribe();
  $('dashboard').hidden = true; $('login').hidden = true; $('logout').hidden = !user;
  if (!user) { $('login').hidden = false; return; }
  try {
    const token = await user.getIdTokenResult(true);
    if (token.claims.admin !== true) { message.textContent = 'Tài khoản này chưa được cấp quyền quản trị.'; await signOut(auth); return; }
    message.textContent = ''; $('dashboard').hidden = false;
    unsubscribe = onSnapshot(query(collection(db, 'orders'), orderBy('createdAt', 'desc')), snapshot => {
      $('orders').replaceChildren(); $('count').textContent = `${snapshot.size} đơn hàng`;
      if (snapshot.empty) $('orders').textContent = 'Chưa có đơn hàng.';
      snapshot.forEach(doc => { const o = doc.data(), card = document.createElement('article'); card.className = 'card';
        const title = document.createElement('h2'); title.textContent = `Đơn #${doc.id.slice(0, 8)} · ${Number(o.total || 0).toLocaleString('vi-VN')} ₫`;
        const details = document.createElement('p'); details.textContent = `${o.items?.[0]?.storeName || 'Cửa hàng chưa xác định'} · ${o.address || ''} · ${o.phone || ''}`;
        const items = document.createElement('p'); items.textContent = (o.items || []).map(i => `${i.name} × ${i.quantity}`).join(', ');
        const status = document.createElement('span'); status.className = 'status'; status.textContent = o.status || ''; card.append(title, details, items, status); $('orders').append(card); });
    }, () => { message.textContent = 'Không tải được đơn hàng. Kiểm tra quyền truy cập Firestore.'; });
  } catch { message.textContent = 'Không thể kiểm tra quyền quản trị.'; }
});
