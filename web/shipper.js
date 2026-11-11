import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js';
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js';
import { getFirestore, collection, query, where, onSnapshot, runTransaction } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-firestore.js';
import { firebaseConfig } from './firebase-config.js';

const $ = id => document.getElementById(id);
const message = $('message');
const app = initializeApp(firebaseConfig, 'shipper');
const auth = getAuth(app), db = getFirestore(app);
let subscriptions = [], authVersion = 0;

$('loginForm').addEventListener('submit', async e => {
  e.preventDefault(); message.textContent = '';
  if (Object.values(firebaseConfig).some(value => !value || value === 'REPLACE_ME')) {
    message.textContent = 'Chưa điền cấu hình Firebase Web trong web/firebase-config.js.'; return;
  }
  try { await signInWithEmailAndPassword(auth, $('email').value.trim(), $('password').value); }
  catch (error) { message.textContent = `Không thể đăng nhập. Kiểm tra tài khoản và kết nối. (${error.code || 'lỗi không xác định'})`; }
});
$('logout').onclick = () => signOut(auth);

function render(list, snapshot, available) {
  list.replaceChildren();
  if (snapshot.empty) { const empty = document.createElement('p'); empty.className = 'empty'; empty.textContent = 'Không có đơn hàng.'; list.append(empty); return; }
  snapshot.forEach(doc => {
    const order = doc.data(), card = document.createElement('article'); card.className = 'card';
    const title = document.createElement('h2'); title.textContent = `Đơn #${doc.id.slice(0, 8)} · ${Number(order.total || 0).toLocaleString('vi-VN')} ₫`;
    const detail = document.createElement('p'); detail.textContent = `${order.items?.[0]?.storeName || 'Cửa hàng chưa xác định'} · ${order.address || ''} · ${order.phone || ''}`;
    const items = document.createElement('p'); items.textContent = (order.items || []).map(item => `${item.name} × ${item.quantity}`).join(', ');
    const status = document.createElement('span'); status.className = 'status'; status.textContent = ({ pending: 'Chờ nhận', accepted: 'Đang giao', delivered: 'Đã giao' })[order.status] || order.status || '';
    card.append(title, detail, items, status);
    if (available || order.status === 'accepted') {
      const action = document.createElement('button'); action.textContent = available ? 'Nhận đơn' : 'Hoàn thành giao hàng';
      action.onclick = async () => {
        action.disabled = true; message.textContent = '';
        try {
          await runTransaction(db, async tx => {
            const current = await tx.get(doc.ref);
            if (!current.exists()) throw new Error('Đơn hàng không còn tồn tại.');
            const data = current.data();
            if (available && data.status === 'pending') tx.update(doc.ref, { status: 'accepted', driverId: auth.currentUser.uid });
            else if (!available && data.status === 'accepted' && data.driverId === auth.currentUser.uid) tx.update(doc.ref, { status: 'delivered' });
            else throw new Error('Đơn hàng đã thay đổi.');
          });
        } catch (error) { message.textContent = `Không cập nhật được đơn: ${error.message}`; }
        finally { action.disabled = false; }
      };
      card.append(document.createElement('br'), action);
    }
    list.append(card);
  });
}

onAuthStateChanged(auth, async user => {
  const version = ++authVersion;
  subscriptions.forEach(unsubscribe => unsubscribe()); subscriptions = [];
  $('dashboard').hidden = true; $('login').hidden = true; $('logout').hidden = !user;
  if (!user) { $('login').hidden = false; return; }
  try {
    const token = await user.getIdTokenResult(true);
    if (version !== authVersion) return;
    if (token.claims.shipper !== true) { message.textContent = 'Tài khoản này chưa được cấp quyền shipper.'; await signOut(auth); return; }
    message.textContent = ''; $('dashboard').hidden = false;
    const orders = collection(db, 'orders');
    const onError = () => { message.textContent = 'Không tải được đơn hàng. Kiểm tra quyền truy cập Firestore.'; };
    subscriptions = [
      onSnapshot(query(orders, where('status', '==', 'pending')), snapshot => render($('available'), snapshot, true), onError),
      onSnapshot(query(orders, where('driverId', '==', user.uid)), snapshot => render($('assigned'), snapshot, false), onError)
    ];
  } catch { if (version === authVersion) { message.textContent = 'Không thể kiểm tra quyền shipper.'; $('login').hidden = false; } }
});
