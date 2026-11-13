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
            --text: #0f172a;
            --muted: #64748b;
            --brand: #ba18f5;
            --brand-soft: #d257ff;
        }

        :root.dark {
            --bg: #0b0b12;
            --text: #f8fafc;
            --muted: #a1a1b5;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 40px;
            padding: 40px 20px;
            overflow-x: hidden;
            background: var(--bg);
            color: var(--text);
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            text-align: center;
            -webkit-font-smoothing: antialiased;
        }

        .hero {
            width: 100%;
            max-width: 380px;
        }

        .brand img {
            display: block;
            height: 26px;
            width: auto;
            margin: 0 auto 28px;
        }

        .code {
            margin: 0;
            font-size: clamp(64px, 12vw, 88px);
            line-height: 1;
            font-weight: 800;
            letter-spacing: -0.06em;
            background: linear-gradient(135deg, var(--brand) 0%, var(--brand-soft) 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            user-select: none;
        }

        h1 {
            margin: 16px 0 8px;
            font-size: 20px;
            font-weight: 700;
            letter-spacing: -0.01em;
        }

        .desc {
            margin: 0 auto;
            max-width: 340px;
            font-size: 14px;
            line-height: 1.7;
            color: var(--muted);
        }

        footer {
            margin: 0;
            font-size: 12px;
            color: var(--muted);
        }
    </style>
</head>
<body>
    <main class="hero">
        <div class="brand">
            <img src="{{ asset('assets/admin/logo-ven.svg') }}" alt="VEN">
        </div>

        <p class="code">404</p>
        <h1>Halaman Tidak Ditemukan</h1>
        <p class="desc">
            Halaman yang Anda tuju tidak tersedia atau sudah dipindahkan.
        </p>
    </main>

    <footer>&copy; {{ date('Y') }} VEN &mdash; Value Exhibition Network</footer>
</body>
</html>
