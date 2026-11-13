<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>404 — Halaman Tidak Ditemukan | VEN</title>
    <link rel="icon" type="image/svg+xml" href="{{ asset('assets/admin/logo-ven.svg') }}">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script>
        (function () {
            var saved = localStorage.getItem('ven_admin_theme');
            var dark = saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches);
            if (dark) {
                document.documentElement.classList.add('dark');
            }
        })();
    </script>
    <style>
        :root {
            --bg: #ffffff;
            --surface: rgba(255, 255, 255, 0.92);
            --border: #ece7f5;
            --text: #0f172a;
            --muted: #64748b;
            --brand: #ba18f5;
            --brand-soft: #d257ff;
            --shadow: 0 24px 60px rgba(88, 28, 135, 0.16);
        }

        :root.dark {
            --bg: #0b0b12;
            --surface: rgba(23, 20, 33, 0.92);
            --border: #2b2440;
            --text: #f8fafc;
            --muted: #a1a1b5;
            --shadow: 0 24px 60px rgba(0, 0, 0, 0.55);
        }

        * {
            box-sizing: border-box;
        }

        html,
        body {
            min-height: 100%;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 24px;
            padding: 32px 20px;
            overflow-x: hidden;
            position: relative;
            background: var(--bg);
            color: var(--text);
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            -webkit-font-smoothing: antialiased;
            transition: background-color 0.25s ease, color 0.25s ease;
        }

        .aurora {
            position: fixed;
            border-radius: 50%;
            pointer-events: none;
            filter: blur(90px);
            opacity: 0.55;
            z-index: 0;
        }

        .aurora-1 {
            width: 620px;
            height: 620px;
            top: -240px;
            left: 50%;
            transform: translateX(-50%);
            background: radial-gradient(circle, rgba(186, 24, 245, 0.55), transparent 65%);
        }

        .aurora-2 {
            width: 460px;
            height: 460px;
            bottom: -200px;
            left: -120px;
            background: radial-gradient(circle, rgba(147, 51, 234, 0.45), transparent 68%);
        }

        .aurora-3 {
            width: 380px;
            height: 380px;
            bottom: -160px;
            right: -100px;
            background: radial-gradient(circle, rgba(192, 132, 252, 0.4), transparent 70%);
        }

        .cube {
            position: fixed;
            z-index: 0;
            width: clamp(90px, 13vw, 170px);
            height: auto;
            pointer-events: none;
            opacity: 0.85;
            animation: float 6s ease-in-out infinite;
        }

        .cube-left {
            left: -28px;
            bottom: -24px;
            transform: rotate(-8deg);
        }

        .cube-right {
            right: -28px;
            bottom: -24px;
            transform: scaleX(-1) rotate(-8deg);
            animation-delay: 1.4s;
        }

        .card {
            position: relative;
            z-index: 1;
            width: 100%;
            max-width: 560px;
            padding: clamp(28px, 5vw, 48px);
            text-align: center;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 28px;
            box-shadow: var(--shadow);
            backdrop-filter: blur(14px);
        }

        .brand {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 18px;
            text-decoration: none;
        }

        .brand img {
            height: 30px;
            width: auto;
        }

        .code {
            margin: 0;
            font-size: clamp(76px, 16vw, 116px);
            line-height: 1;
            font-weight: 800;
            letter-spacing: -0.05em;
            background: linear-gradient(135deg, var(--brand) 0%, var(--brand-soft) 55%, #7e22ce 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            user-select: none;
        }

        h1 {
            margin: 14px 0 10px;
            font-size: clamp(20px, 3vw, 26px);
            font-weight: 700;
            letter-spacing: -0.01em;
        }

        .desc {
            margin: 0 auto;
            max-width: 400px;
            font-size: 14.5px;
            line-height: 1.7;
            color: var(--muted);
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 12px;
            margin-top: 28px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 168px;
            height: 48px;
            padding: 0 24px;
            border-radius: 12px;
            border: 1px solid transparent;
            font-family: inherit;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: transform 0.15s ease, opacity 0.15s ease, background-color 0.15s ease;
        }

        .btn:focus-visible {
            outline: 3px solid rgba(186, 24, 245, 0.35);
            outline-offset: 3px;
        }

        .btn:active {
            transform: scale(0.98);
        }

        .btn-primary {
            background: linear-gradient(90deg, var(--brand) 0%, var(--brand-soft) 100%);
            color: #ffffff;
            box-shadow: 0 12px 26px rgba(186, 24, 245, 0.32);
        }

        .btn-primary:hover {
            opacity: 0.94;
        }

        .btn-ghost {
            background: transparent;
            border-color: var(--border);
            color: var(--text);
        }

        .btn-ghost:hover {
            background: rgba(186, 24, 245, 0.08);
            border-color: rgba(186, 24, 245, 0.45);
        }

        .hint {
            margin: 24px 0 0;
            font-size: 12.5px;
            color: var(--muted);
        }

        .hint strong {
            color: var(--brand);
        }

        footer {
            position: relative;
            z-index: 1;
            margin: 0;
            font-size: 12px;
            color: var(--muted);
            text-align: center;
        }

        @keyframes float {
            0%, 100% {
                translate: 0 0;
            }
            50% {
                translate: 0 -14px;
            }
        }

        @media (prefers-reduced-motion: reduce) {
            .cube {
                animation: none;
            }

            body {
                transition: none;
            }
        }

        @media (max-width: 520px) {
            .cube {
                opacity: 0.35;
            }

            .actions {
                flex-direction: column;
            }

            .btn {
                width: 100%;
            }
        }
    </style>
</head>
<body>
    <div class="aurora aurora-1" aria-hidden="true"></div>
    <div class="aurora aurora-2" aria-hidden="true"></div>
    <div class="aurora aurora-3" aria-hidden="true"></div>

    <img src="{{ asset('assets/admin/balok-2.svg') }}" alt="" aria-hidden="true" class="cube cube-left">
    <img src="{{ asset('assets/admin/balok-2.svg') }}" alt="" aria-hidden="true" class="cube cube-right">

    <main class="card">
        <a class="brand" href="{{ url('/') }}" aria-label="VEN — kembali ke beranda">
            <img src="{{ asset('assets/admin/logo-ven.svg') }}" alt="VEN">
        </a>

        <p class="code" aria-hidden="true">404</p>
        <h1>Halaman Tidak Ditemukan</h1>
        <p class="desc">
            Halaman yang Anda tuju tidak tersedia, sudah dipindahkan, atau alamatnya salah.
            Periksa kembali tautan yang Anda gunakan.
        </p>

        <div class="actions">
            <a class="btn btn-primary" href="{{ url('/') }}">Kembali ke Beranda</a>
            <button class="btn btn-ghost" type="button" onclick="window.location.reload()">Muat Ulang</button>
        </div>

        <p class="hint">Kode status: <strong>404</strong> &middot; Jika masalah berlanjut, hubungi administrator.</p>
    </main>

    <footer>&copy; {{ date('Y') }} VEN &mdash; Virtual Exhibition Navigator</footer>
</body>
</html>
