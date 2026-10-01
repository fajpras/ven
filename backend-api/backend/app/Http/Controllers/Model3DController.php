<?php

namespace App\Http\Controllers;

use App\Models\Model3D;
use Illuminate\Http\Request;

class Model3DController extends Controller
{
    public function index(Request $request)
    {
        $model = Model3D::query()
            ->when($request->enum('jenis', ['hall', 'objek']), fn ($q, $v) => $q->where('jenis', $v))
            ->orderBy('jenis')
            ->orderBy('id_model')
            ->paginate(24);

        return $this->ok($model);
    }

    public function show(Request $request, Model3D $model3d)
    {
        return $this->ok($model3d->load('objek'));
    }
}
