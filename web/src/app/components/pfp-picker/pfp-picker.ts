import { Component, effect, input, output, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-pfp-picker',
  imports: [],
  templateUrl: './pfp-picker.html',
  styleUrl: './pfp-picker.css',
})
export class PfpPicker {
  pfpSrc = input<string | undefined>(undefined);
  initialFile = input<File | undefined | null>(undefined);

  fileSelected = output<File>();

  private internalPreview = signal<string | null>(null);
  fileToLarge = signal(false);

  constructor() {
    effect(() => {
      const file = this.initialFile();
      if (file) {
        this.loadFilePreview(file);
      }
    });
  }

  get pfp() {
    return this.internalPreview() || this.pfpSrc() || environment.defaultPfp;
  }

  handlePfpError(event: any) {
    event.target.src = environment.defaultPfp;
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if(file) {
      if (file.size > 5 * 1024 * 1024) {
          this.fileToLarge.set(true);
          return;
      }

      this.fileToLarge.set(false);
      this.loadFilePreview(file);
      this.fileSelected.emit(file);
    }
  }

  reset() {
    this.internalPreview.set(null);
    this.fileToLarge.set(false);
  }

  private loadFilePreview(file: File) {
    const reader = new FileReader();
    reader.onload = () => {
      this.internalPreview.set(reader.result as string);
    };
    reader.readAsDataURL(file);
  }
}
