import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js';
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js';
import { getFirestore, collection, query, orderBy, onSnapshot } from 'https://www.gstatic.com/firebasejs/11.10.0/firebase-firestore.js';
import { firebaseConfig } from './firebase-config.js';
const $ = id => document.getElementById(id), message = $('message');
const auth = getAuth(initializeApp(firebaseConfig)), db = getFirestore();
let unsubscribe;
$('loginForm').addEventListener('submit', async e => { e.preventDefault(); message.textContent = ''; try { await signInWithEmailAndPassword(auth, $('email').value, $('password').value); } catch { message.textContent = 'Không thể đăng nhập. Kiểm tra tài khoản và cấu hình Firebase.'; } });
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
        const details = document.createElement('p'); details.textContent = `${o.address || ''} · ${o.phone || ''}`;
        const items = document.createElement('p'); items.textContent = (o.items || []).map(i => `${i.name} × ${i.quantity}`).join(', ');
        const status = document.createElement('span'); status.className = 'status'; status.textContent = o.status || ''; card.append(title, details, items, status); $('orders').append(card); });
    }, () => { message.textContent = 'Không tải được đơn hàng. Kiểm tra quyền truy cập Firestore.'; });
  } catch { message.textContent = 'Không thể kiểm tra quyền quản trị.'; }
});
