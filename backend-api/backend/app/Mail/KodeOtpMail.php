<?php

namespace App\Mail;

use App\Enums\TujuanOtp;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;

final class KodeOtpMail extends Mailable
{
    public function __construct(
        public readonly TujuanOtp $tujuan,
        public readonly string $kode,
        public readonly int $berlakuMenit,
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(subject: $this->tujuan->subjekEmail());
    }

    public function content(): Content
    {
        return new Content(view: 'mail.kode-otp');
    }
}
