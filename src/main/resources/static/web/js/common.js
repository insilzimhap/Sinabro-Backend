// /web/js/common.js

// 보호된 API 호출용 공통 fetch
async function apiFetch(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        // 같은 출처(http://localhost:8090)이므로 자동으로 쿠키 붙지만
        // 명시적으로 작성해도 무방
        credentials: "same-origin"
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인이 필요합니다.");
        // 로그인 페이지로 이동
        window.location.replace("/web/html/login/login.html"); // 뒤로가기 루프 방지
        return null; // ⬅️ 호출자가 이걸 보고 추가 처리 중단
    }

    return res;
}
/* ===========================
   공통: 로그인 상태 표시 / 로그아웃
   - header.html 이 삽입된 뒤에 checkLogin()을 호출해줘
   =========================== */

// 헤더에서 로그인 상태 표시
async function checkLogin() {
    try {
        const res = await fetch("/api/admin/auth/me", {
            method: "GET",
            credentials: "same-origin",
        });

        const area = document.getElementById("auth-area");
        if (!area) return; // 헤더 아직 안 붙었으면 그냥 종료

        if (res.ok) {
            const data = await res.json();
            area.innerHTML = `
        <span style="margin-right:10px;">${data.name} (${data.role})</span>
        <button type="button" onclick="logout()">로그아웃</button>
      `;
        } else {
            area.innerHTML = `
        <a href="/web/html/login/login.html">
          <button type="button">로그인</button>
        </a>
      `;
        }
    } catch (e) {
        console.error("세션 확인 실패:", e);
    }
}

// 로그아웃
async function logout() {
    try {
        const res = await fetch("/api/admin/auth/logout", {
            method: "POST",
            credentials: "same-origin",
        });
        if (res.status === 204) {
            alert("로그아웃 되었습니다.");
            window.location.replace("/web/html/login/login.html");
        }
    } catch (e) {
        console.error("로그아웃 실패:", e);
    }
}

