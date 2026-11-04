<?php

namespace App\Mail;

use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;

final class AkunDinonaktifkanMail extends Mailable
{
    public function __construct(
        public readonly string $nama,
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(subject: 'Akun VEN Anda Telah Dinonaktifkan');
    }

    public function content(): Content
    {
        return new Content(view: 'mail.akun-dinonaktifkan');
    }
}
