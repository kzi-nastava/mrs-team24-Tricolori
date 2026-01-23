import { Component, input, output, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-pfp-picker',
  imports: [],
  templateUrl: './pfp-picker.html',
  styleUrl: './pfp-picker.css',
})
export class PfpPicker {
  pfpSrc = input<string | undefined>(undefined);
  fileSelected = output<File>();

  private internalPreview = signal<string | null>(null);
  fileToLarge = signal(false);


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
      const reader = new FileReader();
      reader.onload = () => {
        this.internalPreview.set(reader.result as string);
        this.fileSelected.emit(file);
      }
      reader.readAsDataURL(file);
    }
  }

  get pfp() {
    return this.internalPreview() || this.pfpSrc() || environment.defaultPfp;
  }

  reset() {
    this.internalPreview.set(null);
    this.fileToLarge.set(false);
  }
}
